package fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.KeyframeControllerBase;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeCoordinate;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeGroup;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeTrigger;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.inkAction.AnimationPlayInkAction;
import fr.loudo.narrativecraft.narrative.story.inkAction.InkAction;
import fr.loudo.narrativecraft.narrative.story.inkAction.SubscenePlayInkAction;
import fr.loudo.narrativecraft.screens.cutscenes.CutsceneControllerScreen;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CutsceneController extends KeyframeControllerBase {

    private final AtomicInteger keyframeGroupCounter = new AtomicInteger();
    private final AtomicInteger keyframeCounter = new AtomicInteger();

    private final List<Playback> playbackList;
    private final Cutscene cutscene;
    private final Playback.PlaybackType playbackType;
    private boolean isPlaying;
    private int currentTick;
    private double currentSkipCount;
    private int totalTick;
    private KeyframeGroup selectedKeyframeGroup;
    private StoryHandler storyHandler;
    private List<KeyframeGroup> oldKeyframeGroups;
    private List<KeyframeTrigger> oldKeyframeTriggers;

    public CutsceneController(Cutscene cutscene, ServerPlayer player, Playback.PlaybackType playbackType) {
        super(cutscene.getKeyframeGroupList(), player, playbackType);
        this.cutscene = cutscene;
        this.isPlaying = false;
        this.currentTick = 0;
        this.currentSkipCount = 5 * 20;
        this.playbackList = new ArrayList<>();
        this.playbackType = playbackType;
        initOldData();
    }

    public void initOldData() {
        Cutscene oldCutsceneData = NarrativeCraftFile.getCutsceneData(cutscene);
        if(oldCutsceneData == null) return;
        oldKeyframeGroups = oldCutsceneData.getKeyframeGroupList();
        oldKeyframeTriggers = oldCutsceneData.getKeyframeTriggerList();
    }

    public void startSession() {

        PlayerSession playerSession = NarrativeCraftMod.getInstance().getPlayerSession();
        KeyframeControllerBase keyframeControllerBase = playerSession.getKeyframeControllerBase();
        if(keyframeControllerBase != null) {
            keyframeControllerBase.stopSession(false);
        }
        playerSession.setKeyframeControllerBase(this);

        keyframeGroupCounter.set(cutscene.getKeyframeGroupList().size());

        if(playbackType == Playback.PlaybackType.PRODUCTION) {
            storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
        }

        for(Subscene subscene : cutscene.getSubsceneList()) {
            subscene.start(player.serverLevel(), playbackType, false);
            playbackList.addAll(subscene.getPlaybackList());
        }

        for(Animation animation : cutscene.getAnimationList()) {
            Playback playback = new Playback(animation, player.serverLevel(), animation.getCharacter(), playbackType, false);
            playback.start();
            playbackList.add(playback);
        }


        if(playbackType == Playback.PlaybackType.DEVELOPMENT) {

            this.storyHandler = new StoryHandler();
            storyHandler.setDebugMode(true);
            NarrativeCraftMod.getInstance().setStoryHandler(storyHandler);

            for(KeyframeGroup keyframeGroup : cutscene.getKeyframeGroupList()) {
                for(Keyframe keyframe : keyframeGroup.getKeyframeList()) {
                    keyframe.showKeyframeToClient(player);
                }
                keyframeGroup.getKeyframeList().getFirst().showStartGroupText(player, keyframeGroup.getId());
            }

            for(KeyframeTrigger keyframeTrigger : cutscene.getKeyframeTriggerList()) {
                keyframeTrigger.showKeyframeToClient(player);
            }

            if(!cutscene.getKeyframeGroupList().isEmpty()) {
                selectedKeyframeGroup = cutscene.getKeyframeGroupList().getFirst();
                selectedKeyframeGroup.showGlow(player);
                KeyframeCoordinate keyframeCoordinate = selectedKeyframeGroup.getKeyframeList().getFirst().getKeyframeCoordinate();
                LocalPlayer localPlayer = Minecraft.getInstance().player;
                localPlayer.setPos(keyframeCoordinate.getVec3());
                keyframeCounter.set(cutscene.getKeyframeGroupList().getLast().getKeyframeList().getLast().getId());
            } else if(!playbackList.isEmpty()) {
                LocalPlayer localPlayer = Minecraft.getInstance().player;
                localPlayer.setPos(playbackList.getFirst().getMasterEntity().position());
            }

            pause();
            Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(new CutsceneControllerScreen(this)));
        } else {
            int firstTick = keyframeGroups.getFirst().getKeyframeList().getFirst().getTick();
            if(firstTick > 0) {
                changeTimePosition(firstTick, false);
            }
        }
        totalTick = calculateTotalTick();
    }

    public void stopSession(boolean save) {

        for(Playback playback : playbackList) {
            if(playbackType == Playback.PlaybackType.DEVELOPMENT) {
                playback.forceStop();
            } else if (playbackType == Playback.PlaybackType.PRODUCTION) {
                if(!playback.isLooping()) {
                    playback.stop();
                }
            }
        }

        NarrativeCraftMod.getInstance().getPlaybackHandler().getPlaybacks().removeAll(playbackList.stream().filter(playback -> !playback.isLooping()).toList());

        if(playbackType == Playback.PlaybackType.DEVELOPMENT) {
            storyHandler.getInkActionList().clear();
            NarrativeCraftMod.getInstance().setStoryHandler(null);
            for(KeyframeGroup keyframeGroup : cutscene.getKeyframeGroupList()) {
                for(Keyframe keyframe : keyframeGroup.getKeyframeList()) {
                    keyframe.removeKeyframeFromClient(player);
                }
            }
            for(KeyframeTrigger keyframe : cutscene.getKeyframeTriggerList()) {
                keyframe.removeKeyframeFromClient(player);
            }
            player.setGameMode(GameType.CREATIVE);
        }

        PlayerSession playerSession = NarrativeCraftMod.getInstance().getPlayerSession();
        playerSession.setKeyframeControllerBase(null);
        if(playbackType == Playback.PlaybackType.DEVELOPMENT) {
            if(save) {
                NarrativeCraftFile.updateCutsceneFile(cutscene.getScene());
            } else {
                if(oldKeyframeGroups != null) {
                    cutscene.getKeyframeGroupList().clear();
                    cutscene.getKeyframeGroupList().addAll(oldKeyframeGroups);
                }

                if(oldKeyframeTriggers != null) {
                    cutscene.getKeyframeTriggerList().clear();
                    cutscene.getKeyframeTriggerList().addAll(oldKeyframeTriggers);
                }
            }
        }
        if(playbackType == Playback.PlaybackType.DEVELOPMENT) {
            StoryHandler.changePlayerCutsceneMode(playbackType, false);
        }
        playbackList.clear();
        isPlaying = false;

    }

    public void pause() {
        isPlaying = false;
        if(playbackType == Playback.PlaybackType.DEVELOPMENT) {
            for(InkAction inkAction : storyHandler.getInkActionList()) {
                if(inkAction instanceof SubscenePlayInkAction subscenePlayInkAction) {
                    subscenePlayInkAction.getSubscene().forceStop();
                    NarrativeCraftMod.getInstance().getPlaybackHandler().getPlaybacks().removeAll(subscenePlayInkAction.getSubscene().getPlaybackList());
                }
                if(inkAction instanceof AnimationPlayInkAction animationPlayInkAction) {
                    animationPlayInkAction.getPlayback().forceStop();
                    NarrativeCraftMod.getInstance().getPlaybackHandler().removePlayback(animationPlayInkAction.getPlayback());
                }
            }
            storyHandler.getInkActionList().clear();
            storyHandler.stopAllSound();
        }
        changePlayingPlaybackState();
    }

    public boolean resume() {
        if(currentTick >= totalTick) return false;
        isPlaying = true;
        changePlayingPlaybackState();
        return true;
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

    public void addKeyframeTrigger(String commands, int tick) {
        Vec3 playerPos = Minecraft.getInstance().player.position();
        KeyframeCoordinate keyframeCoordinate = new KeyframeCoordinate(playerPos.x(), playerPos.y() + player.getEyeHeight(), playerPos.z(), player.getXRot(), player.getYRot(), Minecraft.getInstance().options.fov().get());
        keyframeCoordinate.setXRot(0);
        KeyframeTrigger keyframeTrigger = new KeyframeTrigger(keyframeCounter.incrementAndGet(), keyframeCoordinate, tick, commands);
        keyframeTrigger.showKeyframeToClient(player);
        cutscene.getKeyframeTriggerList().add(keyframeTrigger);
    }

    private long getDifferenceSeconds(int tickFirstKeyframe, int tickSecondKeyframe)  {
        int difference = tickSecondKeyframe - tickFirstKeyframe;
        double seconds = difference / 20.0;
        return (long) (seconds * 1000.0);
    }

    public KeyframeTrigger getKeyframeTriggerByEntity(Entity entity) {
        for(KeyframeTrigger keyframe : cutscene.getKeyframeTriggerList()) {
            if(keyframe.getCameraEntity().getId() == entity.getId()) {
                return keyframe;
            }
        }
        return null;
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

    @Override
    public void renderHUDInfo(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        String infoText = Translation.message("cutscene.hud").getString();
        int width = minecraft.getWindow().getGuiScaledWidth();
        guiGraphics.drawString(
                font,
                infoText,
                width / 2 - font.width(infoText) / 2,
                10,
                ChatFormatting.WHITE.getColor()
        );
        String tickInfo = "Tick: " + currentTick + "/" + totalTick;
        guiGraphics.drawString(
                font,
                tickInfo,
                width / 2 - font.width(tickInfo) / 2,
                25,
                ChatFormatting.WHITE.getColor()
        );
    }

    public void removeKeyframeTrigger(KeyframeTrigger keyframeTrigger) {
        cutscene.getKeyframeTriggerList().remove(keyframeTrigger);
        keyframeTrigger.removeKeyframeFromClient(player);
    }

    public void setCurrentPreviewKeyframe(Keyframe currentPreviewKeyframe, boolean seamless) {
        this.currentPreviewKeyframe = currentPreviewKeyframe;
        if(playbackType == Playback.PlaybackType.DEVELOPMENT) {
            currentPreviewKeyframe.openScreenOption(player);
            for(KeyframeGroup keyframeGroup : cutscene.getKeyframeGroupList()) {
                for (Keyframe keyframeFromGroup : keyframeGroup.getKeyframeList()) {
                    keyframeFromGroup.removeKeyframeFromClient(player);
                }
            }
            NarrativeCraftMod.server.execute(() -> changeTimePosition(currentPreviewKeyframe.getTick(), seamless));
        }
        StoryHandler.changePlayerCutsceneMode(playbackType, true);
    }

    public void clearCurrentPreviewKeyframe() {
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
        StoryHandler.changePlayerCutsceneMode(playbackType, false);
        Minecraft.getInstance().options.hideGui = false;
    }

    public void changeTimePosition(int newTick, boolean seamless) {
        currentTick = Math.min(newTick, calculateTotalTick());
        for(Playback playback : playbackList) {
            playback.changeLocationByTick(newTick, seamless);
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
        if(isPlaying) {
            if(playbackType == Playback.PlaybackType.DEVELOPMENT) {
                checkEndedPlayback();
            }
            if(currentPreviewKeyframe != null || playbackType == Playback.PlaybackType.PRODUCTION) {
                List<KeyframeTrigger> keyframeTriggerList = cutscene.getKeyframeTriggerList().stream().filter(keyframeTrigger -> keyframeTrigger.getTick() == currentTick).toList();
                for(KeyframeTrigger keyframeTrigger : keyframeTriggerList) {
                    for(String tag : keyframeTrigger.getCommandsToList()) {
                        storyHandler.getInkTagTranslators().executeTag(tag);
                    }
                }
            }
            currentTick++;
            if(currentTick >= totalTick) {
                if(Minecraft.getInstance().screen instanceof CutsceneControllerScreen cutsceneControllerScreen) {
                    cutsceneControllerScreen.getControllerButton().setMessage(cutsceneControllerScreen.getPlayText());
                }
                isPlaying = false;
            }
        }
    }

    private void changePlayingPlaybackState() {
        for(Subscene subscene : cutscene.getSubsceneList()) {
            for(Playback playback : subscene.getPlaybackList()) {
                playback.setPlaying(isPlaying);
            }
        }
        for(Playback playback : playbackList) {
            playback.setPlaying(isPlaying);
        }
    }

    private void checkEndedPlayback() {
        for(Subscene subscene : cutscene.getSubsceneList()) {
            for(Playback playback : subscene.getPlaybackList()) {
                if(playback.hasEnded() && playback.getMasterEntity() != null) {
                    player.connection.send(new ClientboundHurtAnimationPacket(playback.getMasterEntity()));
                }
            }
        }
        for(Playback playback : playbackList) {
            if(playback.hasEnded() && playback.getMasterEntity() != null) {
                player.connection.send(new ClientboundHurtAnimationPacket(playback.getMasterEntity()));
            }
        }
    }

    private int calculateTotalTick() {
        if (totalTick == 0) {
            int total = 0;
            int count = 0;

            for (Subscene subscene : cutscene.getSubsceneList()) {
                for (Playback playback : subscene.getPlaybackList()) {
                    total += playback.getMaxTick();
                    count++;
                }
            }

            for (Playback playback : playbackList) {
                total += playback.getMaxTick();
                count++;
            }

            if (count == 0) return 0;
            totalTick = total / count;
        }
        return totalTick;
    }

    public int getTotalTick() {
        return totalTick;
    }

    public StoryHandler getStoryHandler() {
        return storyHandler;
    }

    public int getCurrentTick() {
        return currentTick;
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
        Minecraft.getInstance().player.setPos(pos);
    }

    public AtomicInteger getKeyframeGroupCounter() {
        return keyframeGroupCounter;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public double getCurrentSkipCount() {
        return currentSkipCount;
    }

    public Playback.PlaybackType getPlaybackType() {
        return playbackType;
    }

    public List<Playback> getPlaybackList() {
        return playbackList;
    }

    public Animation getAnimationFromEntity(Entity entity) {
        for(Playback playback : playbackList) {
            if(playback.getMasterEntity().getUUID().equals(entity.getUUID())) {
                return playback.getAnimation();
            }
        }
        return null;
    }


}
