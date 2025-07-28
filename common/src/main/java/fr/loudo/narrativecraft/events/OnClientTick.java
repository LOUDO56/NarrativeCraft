package fr.loudo.narrativecraft.events;

import com.mojang.blaze3d.platform.InputConstants;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.keys.ModKeys;
import fr.loudo.narrativecraft.narrative.chapter.scenes.KeyframeControllerBase;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.dialog.Dialog;
import fr.loudo.narrativecraft.narrative.dialog.DialogImpl;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.RecordingHandler;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.MainScreenController;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.inkAction.*;
import fr.loudo.narrativecraft.narrative.story.inkAction.enums.FadeCurrentState;
import fr.loudo.narrativecraft.screens.cameraAngles.CameraAngleControllerScreen;
import fr.loudo.narrativecraft.screens.cutscenes.CutsceneControllerScreen;
import fr.loudo.narrativecraft.screens.mainScreen.MainScreenControllerScreen;
import fr.loudo.narrativecraft.screens.storyManager.chapters.ChaptersScreen;
import fr.loudo.narrativecraft.screens.storyManager.scenes.ScenesMenuScreen;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;

public class OnClientTick {

    private static final RecordingHandler RECORDING_HANDLER = NarrativeCraftMod.getInstance().getRecordingHandler();

    public static void clientTick(Minecraft client) {

        if (client.player == null) return;

        Dialog testDialog = NarrativeCraftMod.getInstance().getTestDialog();
        if (testDialog != null) {
            testDialog.getDialogEntityBobbing().tick();
        }

        // Recording
        ModKeys.handleKeyPress(ModKeys.START_ANIMATION_RECORDING, () -> {
            PlayerSession playerSession = NarrativeCraftMod.getInstance().getPlayerSession();
            if (!playerSession.sessionSet()) {
                client.player.displayClientMessage(Translation.message("session.not_set"), false);
                return;
            }
            Recording recording = NarrativeCraftMod.getInstance().getRecordingHandler().getRecordingOfPlayer(client.player);
            if (recording == null) {
                recording = new Recording(client.player);
            }
            if (!recording.isRecording()) {
                client.player.displayClientMessage(Translation.message("record.start.success"), false);
                recording.start();
            } else {
                client.player.displayClientMessage(Translation.message("record.start.already_recording"), false);
            }
        });

        ModKeys.handleKeyPress(ModKeys.STOP_ANIMATION_RECORDING, () -> {
            PlayerSession playerSession = NarrativeCraftMod.getInstance().getPlayerSession();
            if (!playerSession.sessionSet()) {
                client.player.displayClientMessage(Translation.message("session.not_set"), false);
                return;
            }
            Recording recording = NarrativeCraftMod.getInstance().getRecordingHandler().getRecordingOfPlayer(client.player);
            if (recording == null || !recording.isRecording()) {
                client.player.displayClientMessage(Translation.message("record.stop.no_recording"), false);
            } else {
                recording.stop();
                client.player.displayClientMessage(Translation.message("record.stop.success"), false);
            }
        });

        for (Recording recording : RECORDING_HANDLER.getRecordings()) {
            if (recording.isRecording()) {
                recording.tick();
            }
        }

        // Handle ink action currently playing.
        StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
        if (storyHandler != null) {
            List<InkAction> toRemove = new ArrayList<>();
            List<InkAction> inkActionToLoop = List.copyOf(storyHandler.getInkActionList());
            for (InkAction inkAction : inkActionToLoop) {
                if (inkAction instanceof SongSfxInkAction songSfxInkAction) {
                    if (!songSfxInkAction.isDoneFading() && songSfxInkAction.getFadeCurrentState() != null) {
                        songSfxInkAction.applyFade();
                    }
                    boolean doneFadeAndHasFadedOut = songSfxInkAction.isDoneFading() && songSfxInkAction.getFadeCurrentState() == FadeCurrentState.FADE_OUT;
                    boolean isStillPlaying = client.getSoundManager().isActive(songSfxInkAction.getSimpleSoundInstance());
                    if (doneFadeAndHasFadedOut || !isStillPlaying) {
                        toRemove.add(inkAction);
                    }
                }
                if (inkAction instanceof CooldownInkAction cooldownInkAction) {
                    long now = System.currentTimeMillis();
                    long elapsedTime = now - cooldownInkAction.getStartTime();
                    cooldownInkAction.checkForPause();
                    if (!cooldownInkAction.isPaused()) {
                        if (elapsedTime >= cooldownInkAction.getSecondsToWait()) {
                            storyHandler.getInkTagTranslators().executeLaterTags();
                            toRemove.add(inkAction);
                        }
                    }
                }
                if (inkAction instanceof ShakeScreenInkAction shakeScreenInkAction) {
                    shakeScreenInkAction.tick();
                    if (!shakeScreenInkAction.isShaking()) toRemove.add(shakeScreenInkAction);
                }
                if (inkAction instanceof SubscenePlayInkAction subscenePlayInkAction) {
                    if (subscenePlayInkAction.getSubscene().allPlaybackDone()) {
                        if (subscenePlayInkAction.isBlock()) {
                            storyHandler.getInkTagTranslators().executeLaterTags();
                        }
                        toRemove.add(inkAction);
                    }
                }
                if (inkAction instanceof AnimationPlayInkAction animationPlayInkAction) {
                    if (animationPlayInkAction.getPlayback().hasEnded()) {
                        if (animationPlayInkAction.isBlock()) {
                            storyHandler.getInkTagTranslators().executeLaterTags();
                        }
                        toRemove.add(inkAction);
                    }
                }
            }
            storyHandler.getInkActionList().removeAll(toRemove);
            DialogImpl dialogImpl = storyHandler.getCurrentDialogBox();
            if (dialogImpl instanceof Dialog dialog) {
                dialog.getDialogEntityBobbing().tick();
            }

        }

        // Open story manager screen trigger
        ModKeys.handleKeyPress(ModKeys.OPEN_STORY_MANAGER, () -> {
            if (storyHandler != null && storyHandler.isRunning()) return;
            Screen screen;
            PlayerSession playerSession = NarrativeCraftMod.getInstance().getPlayerSession();
            if (playerSession == null || playerSession.getScene() == null) {
                screen = new ChaptersScreen();
            } else {
                screen = new ScenesMenuScreen(playerSession.getScene());
            }
            client.execute(() -> client.setScreen(screen));
        });


        // Next dialog trigger
        ModKeys.handleKeyPress(ModKeys.NEXT_DIALOG, () -> nextDialog(storyHandler));
        ModKeys.handleKeyPress(InputConstants.MOUSE_BUTTON_LEFT, client.mouseHandler.isLeftPressed(), () -> nextDialog(storyHandler));

        PlayerSession playerSession = NarrativeCraftMod.getInstance().getPlayerSession();
        if (playerSession == null) return;


        // KeyframeControllerBase verification
        KeyframeControllerBase keyframeControllerBase = playerSession.getKeyframeControllerBase();
        if (keyframeControllerBase instanceof CutsceneController cutsceneController) {
            if (cutsceneController.getPlaybackType() == Playback.PlaybackType.PRODUCTION) return;
            ModKeys.handleKeyPress(ModKeys.OPEN_KEYFRAME_EDIT_SCREEN, () -> {
                CutsceneControllerScreen screen = new CutsceneControllerScreen(cutsceneController);
                client.execute(() -> client.setScreen(screen));
            });
        } else if (keyframeControllerBase instanceof MainScreenController mainScreenController) {
            if (mainScreenController.getPlaybackType() == Playback.PlaybackType.PRODUCTION) return;
            ModKeys.handleKeyPress(ModKeys.OPEN_KEYFRAME_EDIT_SCREEN, () -> {
                MainScreenControllerScreen screen = new MainScreenControllerScreen(mainScreenController);
                client.execute(() -> client.setScreen(screen));
            });
        } else if (keyframeControllerBase instanceof CameraAngleController cameraAngleController) {
            if (cameraAngleController.getPlaybackType() == Playback.PlaybackType.PRODUCTION) return;
            ModKeys.handleKeyPress(ModKeys.OPEN_KEYFRAME_EDIT_SCREEN, () -> {
                CameraAngleControllerScreen screen = new CameraAngleControllerScreen(cameraAngleController);
                client.execute(() -> client.setScreen(screen));
            });
        }
    }

    private static void nextDialog(StoryHandler storyHandler) {
        if(storyHandler == null) return;
        DialogImpl dialog = storyHandler.getCurrentDialogBox();
        if(dialog == null) return;
        if(dialog.isAnimating()) return;
        if(dialog.isUnSkippable()) return;
        if(!dialog.getDialogAnimationScrollText().isFinished()) {
            dialog.getDialogAnimationScrollText().forceFinish();
            return;
        }
        KeyframeControllerBase keyframeControllerBase = storyHandler.getPlayerSession().getKeyframeControllerBase();
        if(keyframeControllerBase instanceof CutsceneController) {
            return;
        }
        for(InkAction inkAction : storyHandler.getInkActionList()) {
            if(inkAction instanceof CooldownInkAction) return;
        }
        NarrativeCraftMod.server.execute(storyHandler::next);
    }

}
