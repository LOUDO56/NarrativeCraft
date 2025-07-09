package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.mixin.fields.AbstractHorseFields;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public class AbstractHorseByteAction extends Action {

    private final byte currentByte;
    private final byte oldByte;

    public AbstractHorseByteAction(int tick, byte currentByte, byte oldByte) {
        super(tick, ActionType.ABSTRACT_HORSE_BYTE);
        this.currentByte = currentByte;
        this.oldByte = oldByte;
    }

    @Override
    public void execute(Playback.PlaybackData playbackData) {
        if(playbackData.getEntity() instanceof AbstractHorse abstractHorse) {
            playbackData.getEntity().getEntityData().set(AbstractHorseFields.getDATA_ID_FLAGS(), currentByte);
            if(currentByte >= AbstractHorseFields.getFLAG_STANDING()) {
                if(abstractHorse.getItemBySlot(EquipmentSlot.SADDLE).isEmpty()) {
                    abstractHorse.ejectPassengers();
                }
            }
        }
    }

    @Override
    public void rewind(Playback.PlaybackData playbackData) {
        if(playbackData.getEntity() instanceof AbstractHorse) {
            playbackData.getEntity().getEntityData().set(AbstractHorseFields.getDATA_ID_FLAGS(), oldByte);
        }
    }
}
