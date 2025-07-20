package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import net.minecraft.world.entity.Entity;

public class RidingAction extends Action {

    private final int entityRecordingId;

    public RidingAction(int tick, int entityRecordingId) {
        super(tick, ActionType.RIDE);
        this.entityRecordingId = entityRecordingId;
    }

    @Override
    public void execute(Playback.PlaybackData playbackData) {
        Entity vehicle = playbackData.getPlayback().getEntityByRecordId(entityRecordingId);
        if(vehicle != null) {
            playbackData.getEntity().startRiding(vehicle, true);
        }
    }


    @Override
    public void rewind(Playback.PlaybackData playbackData) {
        playbackData.getEntity().stopRiding();
    }

}
