package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class RidingAction extends Action {

    private final int entityRecordingId;
    private transient List<Playback.PlaybackData> playbackDataList;

    public RidingAction(int tick, int entityRecordingId) {
        super(tick, ActionType.RIDE);
        this.entityRecordingId = entityRecordingId;
    }

    @Override
    public void execute(LivingEntity entity) {
        Entity vehicle = null;
        for(Playback.PlaybackData playbackData : playbackDataList) {
            if(playbackData.getActionsData().getEntityIdRecording() == entityRecordingId) {
                vehicle = playbackData.getEntity();
            }
        }
        if(vehicle != null) {
            entity.startRiding(vehicle, true);
        }
    }


    @Override
    public void rewind(LivingEntity entity) {
        entity.stopRiding();
    }

    public void setPlaybackDataList(List<Playback.PlaybackData> playbackDataList) {
        this.playbackDataList = playbackDataList;
    }
}
