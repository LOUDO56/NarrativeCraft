package fr.loudo.narrativecraft.narrative.chapter.scenes.subscene;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.Cutscene;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.screens.storyManager.scenes.subscenes.SubscenesScreen;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class Subscene extends NarrativeEntry {

    private transient Scene scene;
    private transient List<Animation> animationList;
    private transient List<Playback> playbackList;
    private List<String> animationNameList;

    public Subscene(Scene scene, String name, String description) {
        super(name, description);
        this.scene = scene;
        this.animationList = new ArrayList<>();
        this.animationNameList = new ArrayList<>();
        this.playbackList = new ArrayList<>();
    }

    public void start(ServerPlayer player, Playback.PlaybackType playbackType) {
        if(playbackList == null) {
            playbackList = new ArrayList<>();
        }
        for(String animationName : animationNameList) {
            Animation animation = scene.getAnimationByName(animationName);
            Playback playback = new Playback(animation, player.serverLevel(), animation.getCharacter(), playbackType);
            playback.start();
            playbackList.add(playback);
        }
    }

    public void stop() {
        for(Playback playback : playbackList) {
            playback.stopAndKill();
        }
        playbackList.clear();
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public Scene getScene() {
        return scene;
    }

    public List<Animation> getAnimationList() {
        return animationList;
    }

    public List<Playback> getPlaybackList() {
        return playbackList;
    }

    public List<String> getAnimationNameList() {
        return animationNameList;
    }

    public void setAnimationList(List<Animation> animationList) {
        this.animationList = animationList;
    }

    public void setPlaybackList(List<Playback> playbackList) {
        this.playbackList = playbackList;
    }

    public void setAnimationNameList(List<String> animationNameList) {
        this.animationNameList = animationNameList;
    }

    @Override
    public void update(String name, String description) {
        String oldName = this.name;
        String oldDescription = this.description;
        this.name = name;
        this.description = description;
        if(!NarrativeCraftFile.updateSubsceneFile(scene)) {
            this.name = oldName;
            this.description = oldDescription;
            ScreenUtils.sendToast(Translation.message("toast.error"), Translation.message("screen.subscene_manager.update.failed", name));
            return;
        }
        ScreenUtils.sendToast(Translation.message("toast.info"), Translation.message("toast.description.updated"));
        Minecraft.getInstance().setScreen(reloadScreen());
    }

    @Override
    public void remove() {
        scene.removeSubscene(this);
        for(Cutscene cutscene : scene.getCutsceneList()) {
            cutscene.getSubsceneList().removeIf(subscene -> subscene.getName().equals(name));
        }
        NarrativeCraftFile.updateSubsceneFile(scene);
    }

    @Override
    public Screen reloadScreen() {
        return new SubscenesScreen(scene);
    }
}
