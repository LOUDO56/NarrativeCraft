package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.mixin.fields.LivingEntityFields;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
    public void execute(Entity entity) {
        if(entity instanceof LivingEntity) {
            entity.getEntityData().set(LivingEntityFields.getDATA_LIVING_ENTITY_FLAGS(), livingEntityByte);
        }
    }

    @Override
    public void rewind(Entity entity) {
        if(entity instanceof LivingEntity) {
            entity.getEntityData().set(LivingEntityFields.getDATA_LIVING_ENTITY_FLAGS(), oldLivingEntityByte);
        }
    }
}
