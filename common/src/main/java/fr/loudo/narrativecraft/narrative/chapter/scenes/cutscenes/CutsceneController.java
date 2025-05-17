package fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.KeyframeControllerBase;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeCoordinate;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeGroup;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.screens.cutscenes.CutsceneControllerScreen;
import fr.loudo.narrativecraft.utils.TpUtil;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CutsceneController extends KeyframeControllerBase {

    private final AtomicInteger keyframeGroupCounter = new AtomicInteger();
    private final AtomicInteger keyframeCounter = new AtomicInteger();

    private final Cutscene cutscene;
    private boolean isPlaying;
    private int currentTick;
    private double currentSkipCount;
    private KeyframeGroup selectedKeyframeGroup;

    public CutsceneController(Cutscene cutscene, ServerPlayer player) {
        super(cutscene.getKeyframeGroupList(), player);
        this.cutscene = cutscene;
        this.isPlaying = false;
        this.currentTick = 0;
        this.currentSkipCount = 5 * 20;
    }

    public void startSession() {

        keyframeGroupCounter.set(cutscene.getKeyframeGroupList().size());

        for(Subscene subscene : cutscene.getSubsceneList()) {
            subscene.start(player, Playback.PlaybackType.CUTSCENE_EDITING);
            for(Playback playback : subscene.getPlaybackList()) {
                LivingEntity entity = playback.getEntity();
                for(ServerPlayer serverPlayer : player.serverLevel().getServer().getPlayerList().getPlayers()) {
                    if(!serverPlayer.getName().getString().equals(player.getName().getString())) {
                        player.connection.send(new ClientboundPlayerInfoRemovePacket(List.of(entity.getUUID())));
                    }
                }
            }
        }

        for(KeyframeGroup keyframeGroup : cutscene.getKeyframeGroupList()) {
            for(Keyframe keyframe : keyframeGroup.getKeyframeList()) {
                keyframe.showKeyframeToClient(player);
                keyframesEntity.add(keyframe.getCameraEntity());
            }
        }

        if(!cutscene.getKeyframeGroupList().isEmpty()) {
            selectedKeyframeGroup = cutscene.getKeyframeGroupList().getFirst();
            selectedKeyframeGroup.showGlow(player);
            selectedKeyframeGroup.getKeyframeList().getFirst().showStartGroupText(player, selectedKeyframeGroup.getId());
            TpUtil.teleportPlayer(player, selectedKeyframeGroup.getKeyframeList().getFirst().getKeyframeCoordinate().getVec3());
            keyframeCounter.set(cutscene.getKeyframeGroupList().getLast().getKeyframeList().getLast().getId());
        }

        player.setGameMode(GameType.SPECTATOR);
        pause();
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(new CutsceneControllerScreen(this)));

    }

    public void stopSession() {

        for(Subscene subscene : cutscene.getSubsceneList()) {
            subscene.stop();
        }

        for(KeyframeGroup keyframeGroup : cutscene.getKeyframeGroupList()) {
            for(Keyframe keyframe : keyframeGroup.getKeyframeList()) {
                keyframe.removeKeyframeFromClient(player);
            }
        }

        player.setGameMode(GameType.CREATIVE);

        isPlaying = false;
        PlayerSession playerSession = Utils.getSessionOrNull(player);
        playerSession.setKeyframeControllerBase(null);
        NarrativeCraftFile.updateCutsceneFile(cutscene.getScene());

    }

    public void pause() {
        isPlaying = false;
        changePlayingPlaybackState();
    }

    public void resume() {
        isPlaying = true;
        changePlayingPlaybackState();
    }

    public KeyframeGroup createKeyframeGroup() {
        KeyframeGroup keyframeGroup = new KeyframeGroup(keyframeGroupCounter.incrementAndGet());
        cutscene.getKeyframeGroupList().add(keyframeGroup);
        selectedKeyframeGroup = keyframeGroup;
        addKeyframe();
        updateSelectedGroupGlow();
        return keyframeGroup;
    }

    public void updateSelectedGroupGlow() {
        for(KeyframeGroup keyframeGroup : cutscene.getKeyframeGroupList()) {
            if(keyframeGroup.getId() == selectedKeyframeGroup.getId()) {
                keyframeGroup.showGlow(player);
            } else {
                keyframeGroup.removeGlow(player);
            }
        }
    }

    public boolean addKeyframe() {
        if(selectedKeyframeGroup == null) return false;
        int newId = keyframeCounter.incrementAndGet();
        Vec3 playerPos = player.position();
        KeyframeCoordinate keyframeCoordinate = new KeyframeCoordinate(playerPos.x(), playerPos.y() + player.getEyeHeight(), playerPos.z(), player.getXRot(), player.getYRot(), Minecraft.getInstance().options.fov().get());
        Keyframe keyframe = new Keyframe(newId, keyframeCoordinate, currentTick, 0, 0);
        keyframe.showKeyframeToClient(player);
        if(!selectedKeyframeGroup.getKeyframeList().isEmpty()) {
            long pathTime = getDifferenceSeconds(selectedKeyframeGroup.getKeyframeList().getLast().getTick(),keyframe.getTick());
            keyframe.setPathTime(pathTime);
        } else {
            keyframe.showStartGroupText(player, selectedKeyframeGroup.getId());
            if(selectedKeyframeGroup.getId() > 1) {
                Keyframe lastKeyframe = cutscene.getKeyframeGroupList().get(keyframeGroupCounter.get() - 2).getKeyframeList().getLast();
                long transitionDelay = getDifferenceSeconds(lastKeyframe.getTick(), keyframe.getTick());
                lastKeyframe.setTransitionDelay(transitionDelay);
            }
        }
        selectedKeyframeGroup.getKeyframeList().add(keyframe);
        selectedKeyframeGroup.showGlow(player);
        return true;
    }

    private long getDifferenceSeconds(int tickFirstKeyframe, int tickSecondKeyframe)  {
        int difference = tickSecondKeyframe - tickFirstKeyframe;
        double seconds = difference / 20.0;
        return (long) (seconds * 1000.0);
    }

    public boolean removeKeyframe(Keyframe keyframe) {
        for(KeyframeGroup keyframeGroup : cutscene.getKeyframeGroupList()) {
            for(Keyframe keyframeFromGroup : keyframeGroup.getKeyframeList()) {
                if(keyframe.getId() == keyframeFromGroup.getId()) {
                    keyframe.removeKeyframeFromClient(player);
                    List<Keyframe> keyframeList = keyframeGroup.getKeyframeList();
                    keyframeList.remove(keyframeFromGroup);
                    if(!keyframeList.isEmpty()) {
                        // If the user delete the first keyframe from the group, then set the first keyframe group to the next keyframe
                        Keyframe newFirstKeyframeGroup = keyframeList.getFirst();
                        newFirstKeyframeGroup.showStartGroupText(player, keyframeGroup.getId());
                    } else {
                        // Re-assign automatically the group id if the user delete a keyframe group without any child
                        keyframeGroupCounter.decrementAndGet();
                        cutscene.getKeyframeGroupList().remove(keyframeGroup);
                        for(int i = 0; i < cutscene.getKeyframeGroupList().size(); i++) {
                            KeyframeGroup keyframeGroup1 = cutscene.getKeyframeGroupList().get(i);
                            keyframeGroup1.getKeyframeList().getFirst().showStartGroupText(player, i + 1);
                            keyframeGroup1.setId(i + 1);
                        }
                        if(!cutscene.getKeyframeGroupList().isEmpty()) {
                            setSelectedKeyframeGroup(cutscene.getKeyframeGroupList().getLast());
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public void setCurrentPreviewKeyframe(Keyframe currentPreviewKeyframe, boolean seamless) {
        this.currentPreviewKeyframe = currentPreviewKeyframe;
        currentPreviewKeyframe.openScreenOption(player);
        for(KeyframeGroup keyframeGroup : cutscene.getKeyframeGroupList()) {
            for (Keyframe keyframeFromGroup : keyframeGroup.getKeyframeList()) {
                keyframeFromGroup.removeKeyframeFromClient(player);
            }
        }
        changeTimePosition(currentPreviewKeyframe.getTick(), seamless);
    }

    public void clearCurrentPreviewKeyframe() {
        Minecraft.getInstance().options.hideGui = false;
        for(KeyframeGroup keyframeGroup : cutscene.getKeyframeGroupList()) {
            for (Keyframe keyframeFromGroup : keyframeGroup.getKeyframeList()) {
                keyframeFromGroup.showKeyframeToClient(player);
                if(keyframeFromGroup.isParentGroup()) {
                    keyframeFromGroup.showStartGroupText(player, keyframeGroup.getId());
                }
            }
        }
        selectedKeyframeGroup.showGlow(player);
        currentPreviewKeyframe = null;
    }

    public void changeTimePosition(int newTick, boolean seamless) {
        currentTick = newTick;
        for(Subscene subscene : cutscene.getSubsceneList()) {
            for(Playback playback : subscene.getPlaybackList()) {
                playback.changeLocationByTick(newTick, seamless);
            }
        }
    }

    public boolean isLastKeyframe(Keyframe keyframe) {
        return keyframeGroupCounter.get() == cutscene.getKeyframeGroupList().getLast().getId()
                && cutscene.getKeyframeGroupList().getLast().getKeyframeList().getLast().getId() == keyframe.getId();
    }

    public void nextSecondSkip() {
        changeTimePosition(currentTick + (int) currentSkipCount, true);
    }

    public void previousSecondSkip() {
        changeTimePosition(Math.max(0, currentTick - (int) currentSkipCount), true);
    }

    public void next() {
        if(isPlaying) currentTick++;
        checkEndedPlayback();
    }

    private void changePlayingPlaybackState() {
        for(Subscene subscene : cutscene.getSubsceneList()) {
            for(Playback playback : subscene.getPlaybackList()) {
                playback.setPlaying(isPlaying);
            }
        }
    }

    private void checkEndedPlayback() {
        for(Subscene subscene : cutscene.getSubsceneList()) {
            for(Playback playback : subscene.getPlaybackList()) {
                if(playback.hasEnded()) {
                    player.connection.send(new ClientboundHurtAnimationPacket(playback.getEntity()));
                }
            }
        }
    }

    public Cutscene getCutscene() {
        return cutscene;
    }

    public void setCurrentSkipCount(double currentSkipCount) {
        this.currentSkipCount = currentSkipCount * 20;
    }

    public KeyframeGroup getSelectedKeyframeGroup() {
        return selectedKeyframeGroup;
    }

    public void setSelectedKeyframeGroup(KeyframeGroup selectedKeyframeGroup) {
        this.selectedKeyframeGroup = selectedKeyframeGroup;
        updateSelectedGroupGlow();
        Vec3 pos = selectedKeyframeGroup.getKeyframeList().getFirst().getKeyframeCoordinate().getVec3();
        TpUtil.teleportPlayer(player, pos.x(), pos.y() - 1, pos.z());
    }

    public AtomicInteger getKeyframeGroupCounter() {
        return keyframeGroupCounter;
    }

    public List<Entity> getKeyframesEntity() {
        return keyframesEntity;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public double getCurrentSkipCount() {
        return currentSkipCount;
    }

}
