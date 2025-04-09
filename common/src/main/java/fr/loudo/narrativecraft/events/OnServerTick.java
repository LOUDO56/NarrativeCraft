package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.RecordingHandler;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.recordings.playback.PlaybackHandler;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import net.minecraft.server.level.ServerPlayer;

public class OnServerTick {

    private static final RecordingHandler RECORDING_HANDLER = NarrativeCraftMod.getInstance().getRecordingHandler();
    private static final PlaybackHandler PLAYBACK_HANDLER = NarrativeCraftMod.getInstance().getPlaybackHandler();

    public static void serverTick() {
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
            }
        }
    }
}
