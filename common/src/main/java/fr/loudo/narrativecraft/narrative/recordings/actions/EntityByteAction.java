package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.LivingEntity;

public class EntityByteAction extends Action {

    private byte entityByte;
    private byte previousEntityByte;

    public EntityByteAction(int waitTick, ActionType actionType, byte entityByte, byte previousEntityByte) {
        super(waitTick, actionType);
        this.entityByte = entityByte;
        this.previousEntityByte = previousEntityByte;
    }

    @Override
    public void execute(LivingEntity entity) {
        SynchedEntityData entityData = entity.getEntityData();
        EntityDataAccessor<Byte> ENTITY_BYTE_MASK = new EntityDataAccessor<>(0, EntityDataSerializers.BYTE);
        entityData.set(ENTITY_BYTE_MASK, entityByte);
    }

    public void execute(LivingEntity entity, boolean previousOne) {
        SynchedEntityData entityData = entity.getEntityData();
        EntityDataAccessor<Byte> ENTITY_BYTE_MASK = new EntityDataAccessor<>(0, EntityDataSerializers.BYTE);
        entityData.set(ENTITY_BYTE_MASK, previousEntityByte);
    }
}
