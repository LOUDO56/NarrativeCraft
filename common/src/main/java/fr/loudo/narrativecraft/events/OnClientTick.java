package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.keys.ModKeys;
import fr.loudo.narrativecraft.narrative.chapter.scenes.KeyframeControllerBase;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeGroup;
import fr.loudo.narrativecraft.narrative.dialog.Dialog;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.inkAction.InkAction;
import fr.loudo.narrativecraft.narrative.story.inkAction.SongSfxInkAction;
import fr.loudo.narrativecraft.narrative.story.inkAction.WaitInkAction;
import fr.loudo.narrativecraft.screens.cameraAngles.CameraAngleControllerScreen;
import fr.loudo.narrativecraft.screens.cameraAngles.CameraAngleInfoKeyframeScreen;
import fr.loudo.narrativecraft.screens.cutscenes.CutsceneControllerScreen;
import fr.loudo.narrativecraft.screens.storyManager.chapters.ChaptersScreen;
import fr.loudo.narrativecraft.screens.storyManager.scenes.ScenesMenuScreen;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;

public class OnClientTick {

    public static void clientTick(Minecraft client) {

        if(client.player == null) return;

        // Handle ink action currently playing.
        StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
        if(storyHandler != null && storyHandler.isRunning()) {
            List<InkAction> toRemove = new ArrayList<>();
            List<InkAction> inkActionToLoop = List.copyOf(storyHandler.getInkActionList());
            for (InkAction inkAction : inkActionToLoop) {
                if (inkAction instanceof SongSfxInkAction songSfxInkAction) {
                    if(!songSfxInkAction.isDoneFading()) {
                        songSfxInkAction.applyFade();
                    }
                    boolean doneFadeAndHasFadedOut = songSfxInkAction.isDoneFading() && songSfxInkAction.getFadeCurrentState() == StoryHandler.FadeCurrentState.FADE_OUT;
                    boolean isStillPlaying = client.getSoundManager().isActive(songSfxInkAction.getSimpleSoundInstance());
                    if (doneFadeAndHasFadedOut || !isStillPlaying) {
                        toRemove.add(inkAction);
                    }
                }
                if(inkAction instanceof WaitInkAction waitInkAction) {
                    long now = System.currentTimeMillis();
                    long elapsedTime = now - waitInkAction.getStartTime();
                    waitInkAction.checkForPause();
                    if(!waitInkAction.isPaused()) {
                        if(elapsedTime >= waitInkAction.getSecondsToWait()) {
                            storyHandler.getInkTagTranslators().executeLaterTags();
                            toRemove.add(inkAction);
                        }
                    }
                }
            }
            storyHandler.getInkActionList().removeAll(toRemove);

        }

        // Open story manager screen trigger
        ModKeys.handleKeyPress(ModKeys.OPEN_STORY_MANAGER, () -> {
            Screen screen;
            PlayerSession playerSession = Utils.getSessionOrNull(client.player.getUUID());
            if(playerSession == null) {
                screen = new ChaptersScreen();
            } else {
                screen = new ScenesMenuScreen(playerSession.getScene());
            }
            client.execute(() -> client.setScreen(screen));
        });


        // Next dialog trigger
        ModKeys.handleKeyPress(ModKeys.NEXT_DIALOG, () -> {
            if(storyHandler == null) return;
            Dialog dialog = storyHandler.getCurrentDialogBox();
            if(dialog == null) return;
            if(dialog.isAnimating()) return;
            if(dialog.isUnskippable()) return;
            if(!dialog.getDialogScrollText().isFinished()) {
                dialog.getDialogScrollText().forceFinish();
                return;
            }
            KeyframeControllerBase keyframeControllerBase = storyHandler.getPlayerSession().getKeyframeControllerBase();
            if(keyframeControllerBase instanceof CutsceneController) {
                return;
            }
            for(InkAction inkAction : storyHandler.getInkActionList()) {
                if(inkAction instanceof WaitInkAction) return;
            }
            storyHandler.next();
        });

        PlayerSession playerSession = Utils.getSessionOrNull(client.player.getUUID());
        if(playerSession == null) return;


        // KeyframeControllerBase verification
        KeyframeControllerBase keyframeControllerBase = playerSession.getKeyframeControllerBase();
        if(keyframeControllerBase instanceof CutsceneController cutsceneController) {
            if(cutsceneController.getPlaybackType() == Playback.PlaybackType.PRODUCTION) return;
            ModKeys.handleKeyPress(ModKeys.CREATE_KEYFRAME_GROUP, () -> {
                KeyframeGroup keyframeGroup = cutsceneController.createKeyframeGroup();
                Minecraft.getInstance().player.displayClientMessage(Translation.message("cutscene.keyframegroup.created", keyframeGroup.getId()), false);
            });
            ModKeys.handleKeyPress(ModKeys.ADD_KEYFRAME, () -> {
                if (cutsceneController.addKeyframe()) {
                    playerSession.getPlayer().sendSystemMessage(
                            Translation.message("cutscene.keyframe.added", cutsceneController.getSelectedKeyframeGroup().getId())
                    );
                } else {
                    playerSession.getPlayer().sendSystemMessage(
                            Translation.message("cutscene.keyframe.added.fail")
                    );
                }
            });
            ModKeys.handleKeyPress(ModKeys.OPEN_KEYFRAME_EDIT_SCREEN, () -> {
                CutsceneControllerScreen screen = new CutsceneControllerScreen(cutsceneController);
                client.execute(() -> client.setScreen(screen));
            });
        }
        if(keyframeControllerBase instanceof CameraAngleController cameraAngleController) {
            if(cameraAngleController.getPlaybackType() == Playback.PlaybackType.PRODUCTION) return;
            ModKeys.handleKeyPress(ModKeys.ADD_KEYFRAME, () -> {
                CameraAngleInfoKeyframeScreen screen = new CameraAngleInfoKeyframeScreen(cameraAngleController);
                client.execute(() -> client.setScreen(screen));
            });
            ModKeys.handleKeyPress(ModKeys.OPEN_KEYFRAME_EDIT_SCREEN, () -> {
                CameraAngleControllerScreen screen = new CameraAngleControllerScreen(cameraAngleController);
                client.execute(() -> client.setScreen(screen));
            });
        }
    }

}
