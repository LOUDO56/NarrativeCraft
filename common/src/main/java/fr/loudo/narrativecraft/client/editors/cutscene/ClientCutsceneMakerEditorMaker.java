/*
 * NarrativeCraft - Create narrative games inside Minecraft. No coding, no game engine, only text and logic.
 * Copyright (c) 2025 LOUDO and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package fr.loudo.narrativecraft.client.editors.cutscene;

import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Keyframe;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.CutsceneLayer;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.ICutsceneLayer;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import fr.loudo.narrativecraft.narrative.cutscene.CutsceneDeserializer;
import fr.loudo.narrativecraft.narrative.cutscene.CutsceneSerializer;
import fr.loudo.narrativecraft.network.cutscene.BiCutscenePlayHeadPacket;
import fr.loudo.narrativecraft.network.cutscene.C2SCutsceneControl;
import fr.loudo.narrativecraft.network.cutscene.C2SCutsceneSave;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.utils.UtilsClient;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;

public class ClientCutsceneMakerEditorMaker implements EditorMaker {

    private final Minecraft mc = Minecraft.getInstance();
    private final List<CutsceneLayer> layers = new ArrayList<>();
    private final Cutscene cutscene;
    private final ClientPlayerSession playerSession =
            ClientNarrativeCraftMod.getInstance().getPlayerSession();
    private final CutsceneEditorPlayback playback;
    private final CutsceneMakerEditorShortcuts shortcuts = new CutsceneMakerEditorShortcuts(this);
    private final NarrativeEnvironment environment;

    private final List<Keyframe> selectedKeyframes = new ArrayList<>();

    private int totalTick;
    private int playHeadTick = 0;
    private float previewRoll = 0f;
    private boolean renderingHud = true;

    public ClientCutsceneMakerEditorMaker(Cutscene cutscene, NarrativeEnvironment environment) {
        this.cutscene = cutscene;
        this.playback = new CutsceneEditorPlayback(layers, playerSession, cutscene.getMaxTick());
        this.environment = environment;
    }

    @Override
    public void init() {
        totalTick = cutscene.getMaxTick();
        playback.setTotalTick(totalTick);
        playHeadTick = 0;

        if (environment == NarrativeEnvironment.PRODUCTION) {
            startProductionPlayback();
        }
    }

    private void startProductionPlayback() {
        if (environment != NarrativeEnvironment.PRODUCTION) return;
        int firstCameraTick = cutscene.getFirstCameraTick();
        playback.play(firstCameraTick < totalTick ? firstCameraTick : 0);
    }

    public void applyManualMaxTick(int value) {
        if (value <= 0) return;
        cutscene.setManualMaxTick(value);
        totalTick = value;
        playback.setTotalTick(value);
    }

    public void save() {
        String layersJson = CutsceneSerializer.serializeLayers(layers);
        Services.PACKET.sendToServer(new C2SCutsceneSave(cutscene, layersJson));
    }

    public void quit(boolean saveBeforeQuit) {
        if (saveBeforeQuit) {
            save();
        }
        Services.PACKET.sendToServer(new C2SCutsceneControl(C2SCutsceneControl.State.QUIT));
        playerSession.closeEditor();
    }

    @Override
    public void close() {
        playback.pause();
        playerSession.getCutsceneDataSession().reset();
        UtilsClient.setHudHidden(false);
        for (CutsceneLayer layer : layers) {
            layer.stop();
        }
        if (environment != NarrativeEnvironment.DEVELOPMENT) return;
        mc.gui.setScreen(null);
        playerSession.stopAllClientInkActions();
    }

    @Override
    public void tick() {
        boolean hideGui = Minecraft.getInstance().gui.hud.isHidden();
        if (playback.isPlaying() && !hideGui) {
            UtilsClient.setHudHidden(true);
        } else if (!playback.isPlaying() && hideGui) {
            UtilsClient.setHudHidden(false);
        }
    }

    @Override
    public void teleportToEditorOrigin() {}

    @Override
    public void keyPressed(KeyEvent event) {
        if (!renderingHud || environment != NarrativeEnvironment.DEVELOPMENT) return;
        shortcuts.handleKeyPressed(event);
    }

    public void loadLayers(String layersJson) {
        layers.clear();
        CutsceneDeserializer.deserializeLayers(layersJson, cutscene);
        if (cutscene.getLayers() != null) {
            layers.addAll(cutscene.getLayers());
        }
        totalTick = cutscene.getMaxTick();
        playback.setTotalTick(totalTick);
        startProductionPlayback();
    }

    public void addLayer(CutsceneLayer layer) {
        layers.add(layer);
        rebuildSortIndices();
    }

    public void removeLayer(ICutsceneLayer layer) {
        layers.remove(layer);
        rebuildSortIndices();
    }

    public void toggleHud() {
        renderingHud = !renderingHud;
    }

    public void clearSelection() {
        for (Keyframe keyframe : selectedKeyframes) {
            keyframe.setSelected(false);
        }
        selectedKeyframes.clear();
    }

    public void startPlayback() {
        if (playback.getCurrentTick() >= totalTick) {
            setPlayHeadTick(0);
        }
        playback.play(playback.getCurrentTick());
    }

    public void pausePlayback() {
        playback.pause();
        setPreviewRoll(0f);
    }

    private void rebuildSortIndices() {
        for (int i = 0; i < layers.size(); i++) {
            layers.get(i).setSortIndex(i);
        }
    }

    public int getPlayHeadTick() {
        return playHeadTick;
    }

    public void setPlayHeadTick(int tick) {
        playHeadTick = (int) Math.clamp(tick, 0, totalTick);
        playback.seekTo(playHeadTick);
        Services.PACKET.sendToServer(new BiCutscenePlayHeadPacket(playHeadTick));
    }

    public float getTick() {
        return playback.getCurrentTick();
    }

    public int getTotalTick() {
        return totalTick;
    }

    public Cutscene getCutscene() {
        return cutscene;
    }

    public CutsceneEditorPlayback getPlayback() {
        return playback;
    }

    public CutsceneMakerEditorShortcuts getShortcuts() {
        return shortcuts;
    }

    public boolean isRenderingHud() {
        return renderingHud;
    }

    public void setRenderingHud(boolean renderingHud) {
        this.renderingHud = renderingHud;
    }

    public float getPreviewRoll() {
        return previewRoll;
    }

    public void setPreviewRoll(float roll) {
        this.previewRoll = roll;
    }

    public List<CutsceneLayer> getLayers() {
        return layers;
    }

    public List<Keyframe> getSelectedKeyframes() {
        return selectedKeyframes;
    }

    @Override
    public NarrativeEnvironment getEnvironment() {
        return environment;
    }
}
