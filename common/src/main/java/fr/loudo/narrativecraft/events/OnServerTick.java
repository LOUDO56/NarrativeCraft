package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.KeyframeControllerBase;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.RecordingHandler;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.recordings.playback.PlaybackHandler;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class OnServerTick {

    private static final PlaybackHandler PLAYBACK_HANDLER = NarrativeCraftMod.getInstance().getPlaybackHandler();
    private static final RecordingHandler RECORDING_HANDLER = NarrativeCraftMod.getInstance().getRecordingHandler();

    public static void serverTick() {
        if(Minecraft.getInstance().isPaused()) return;

        for (Recording recording : RECORDING_HANDLER.getRecordings()) {
            if (recording.isRecording()) {
                recording.tick();
            }
        }

        List<Playback> playbackEnded = new ArrayList<>();
        for (Playback playback : PLAYBACK_HANDLER.getPlaybacks()) {
            if (playback.isPlaying()) {
                playback.next();
            }
            if(playback.hasEnded()) playbackEnded.add(playback);
        }
        PLAYBACK_HANDLER.getPlaybacks().removeAll(playbackEnded);

        PlayerSession playerSession = NarrativeCraftMod.getInstance().getPlayerSession();
        KeyframeControllerBase keyframeControllerBase = playerSession.getKeyframeControllerBase();
        if(keyframeControllerBase instanceof CutsceneController cutsceneController) {
            cutsceneController.next();
        }
    }
}
