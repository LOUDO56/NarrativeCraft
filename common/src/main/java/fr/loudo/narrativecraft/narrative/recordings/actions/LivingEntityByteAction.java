package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.LivingEntity;

public class LivingEntityByteAction extends Action {

    private byte livingEntityByte;

    public LivingEntityByteAction(int waitTick, byte livingEntityByte) {
        super(waitTick, ActionType.LIVING_ENTITY_BYTE);
        this.livingEntityByte = livingEntityByte;
    }

    @Override
    public void execute(LivingEntity entity) {
        SynchedEntityData entityData = entity.getEntityData();
        EntityDataAccessor<Byte> LIVING_ENTITY_BYTE_MASK = new EntityDataAccessor<>(8, EntityDataSerializers.BYTE);
        entityData.set(LIVING_ENTITY_BYTE_MASK, livingEntityByte);
    }

    @Override
    public void rewind(LivingEntity entity) {}
}
