package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.KeyframeControllerBase;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.RecordingHandler;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.recordings.playback.PlaybackHandler;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.inkAction.ChangeDayTimeInkAction;
import fr.loudo.narrativecraft.narrative.story.inkAction.InkAction;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        Iterator<Playback> iterator = PLAYBACK_HANDLER.getPlaybacks().iterator();
        while (iterator.hasNext()) {
            Playback playback = iterator.next();
            if (playback.isPlaying()) {
                playback.next();
            }
        }

        for(PlayerSession playerSession : NarrativeCraftMod.getInstance().getPlayerSessionManager().getPlayerSessions()) {
            KeyframeControllerBase keyframeControllerBase = playerSession.getKeyframeControllerBase();
            if(keyframeControllerBase instanceof CutsceneController cutsceneController) {
                cutsceneController.next();
            }
        }
    }
}
