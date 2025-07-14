package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.MovementData;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;

public class RespawnAction extends Action{

    private final MovementData respawnPos;

    public RespawnAction(int tick, MovementData respawnPos) {
        super(tick, ActionType.RESPAWN);
        this.respawnPos = respawnPos;
    }

    @Override
    public void execute(Playback.PlaybackData playbackData) {
        playbackData.getPlayback().killMasterEntity();
        playbackData.getPlayback().respawnMasterEntity(respawnPos);
    }

    @Override
    public void rewind(Playback.PlaybackData playbackData) {
        playbackData.getPlayback().killMasterEntity();
    }
}
