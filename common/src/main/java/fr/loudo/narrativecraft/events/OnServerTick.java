package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutscenePlayback;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeGroup;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.RecordingHandler;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.recordings.playback.PlaybackHandler;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeCoordinate;
import fr.loudo.narrativecraft.utils.TpUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;

public class OnServerTick {

    private static final RecordingHandler RECORDING_HANDLER = NarrativeCraftMod.getInstance().getRecordingHandler();
    private static final PlaybackHandler PLAYBACK_HANDLER = NarrativeCraftMod.getInstance().getPlaybackHandler();

    public static void serverTick() {
        if(Minecraft.getInstance().isPaused()) return;
        for(Recording recording : RECORDING_HANDLER.getRecordings()) {
            if(recording.isRecording()) {
                ServerPlayer player = recording.getPlayer();
                recording.getActionsData().addMovement(player);
                recording.getActionDifference().listenDifference();
            }
        }
        for(Playback playback : PLAYBACK_HANDLER.getPlaybacks()) {
            if(playback.isPlaying()) {
                playback.next();
            }
        }
        for(PlayerSession playerSession : NarrativeCraftMod.getInstance().getPlayerSessionManager().getPlayerSessions()) {
            CutsceneController cutsceneController = playerSession.getCutsceneController();
            if(cutsceneController != null) {
                cutsceneController.next();
                if(NarrativeCraftMod.server.getTickCount() % 5 == 0 && cutsceneController.getCurrentPreviewKeyframe() == null) {
                    for(KeyframeGroup keyframeGroup : cutsceneController.getCutscene().getKeyframeGroupList()) {
                        keyframeGroup.showLineBetweenKeyframes(playerSession.getPlayer());
                    }
                }
                Keyframe keyframePreview = cutsceneController.getCurrentPreviewKeyframe();
                if(keyframePreview != null) {
                    KeyframeCoordinate position = keyframePreview.getKeyframeCoordinate();
                    TpUtil.teleportPlayer(playerSession.getPlayer(), position.getX(), position.getY(), position.getZ());
                }
            }
            CutscenePlayback cutscenePlayback = playerSession.getCutscenePlayback();
            if(cutscenePlayback != null) {
                KeyframeCoordinate currentLoc = cutscenePlayback.getCurrentLoc();
                if(currentLoc != null) {
                    // Teleport player every tick to handle chunk loading while camera moves.
                    TpUtil.teleportPlayer(playerSession.getPlayer(), currentLoc.getX(), currentLoc.getY(), currentLoc.getZ());
                }
            }
        }
    }
}
