package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftManager;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.RecordingHandler;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.recordings.playback.PlaybackHandler;
import net.minecraft.server.level.ServerPlayer;

public class OnServerTick {

    private static final RecordingHandler RECORDING_HANDLER = NarrativeCraftManager.getInstance().getRecordingHandler();
    private static final PlaybackHandler PLAYBACK_HANDLER = NarrativeCraftManager.getInstance().getPlaybackHandler();

    public static void serverTick() {
        for(Recording recording : RECORDING_HANDLER.getRecordings()) {
            if(recording.isRecording()) {
                recording.addTickAction();
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
    }
}
