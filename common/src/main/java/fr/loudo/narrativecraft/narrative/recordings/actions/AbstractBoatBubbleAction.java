package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.mixin.fields.AbstractBoatFields;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractBoat;

public class AbstractBoatBubbleAction extends Action {

    private final int currentByte;
    private final int oldByte;

    public AbstractBoatBubbleAction(int tick, int currentByte, int oldByte) {
        super(tick, ActionType.ABSTRACT_BOAT_BUBBLE);
        this.currentByte = currentByte;
        this.oldByte = oldByte;
    }

    @Override
    public void execute(Playback.PlaybackData playbackData) {
        Entity entity1 = playbackData.getEntity().level().getEntity(playbackData.getEntity().getId());
        if(entity1 instanceof AbstractBoat) {
            playbackData.getEntity().getEntityData().set(AbstractBoatFields.getDATA_ID_BUBBLE_TIME(), currentByte);
        }
    }

    @Override
    public void rewind(Playback.PlaybackData playbackData) {
        Entity entity1 = playbackData.getEntity().level().getEntity(playbackData.getEntity().getId());
        if(entity1 instanceof AbstractBoat) {
            playbackData.getEntity().getEntityData().set(AbstractBoatFields.getDATA_ID_BUBBLE_TIME(), oldByte);
        }
    }
}
