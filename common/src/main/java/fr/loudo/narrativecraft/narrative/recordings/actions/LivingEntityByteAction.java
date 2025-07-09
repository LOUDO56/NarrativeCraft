package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.mixin.fields.LivingEntityFields;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class LivingEntityByteAction extends Action {

    private final byte livingEntityByte;
    private final byte oldLivingEntityByte;

    public LivingEntityByteAction(int waitTick, byte livingEntityByte, byte oldLivingEntityByte) {
        super(waitTick, ActionType.LIVING_ENTITY_BYTE);
        this.livingEntityByte = livingEntityByte;
        this.oldLivingEntityByte = oldLivingEntityByte;
    }

    @Override
    public void execute(Playback.PlaybackData playbackData) {
        if(playbackData.getEntity() instanceof LivingEntity) {
            playbackData.getEntity().getEntityData().set(LivingEntityFields.getDATA_LIVING_ENTITY_FLAGS(), livingEntityByte);
        }
    }

    @Override
    public void rewind(Playback.PlaybackData playbackData) {
        if(playbackData.getEntity() instanceof LivingEntity) {
            playbackData.getEntity().getEntityData().set(LivingEntityFields.getDATA_LIVING_ENTITY_FLAGS(), oldLivingEntityByte);
        }
    }
}
