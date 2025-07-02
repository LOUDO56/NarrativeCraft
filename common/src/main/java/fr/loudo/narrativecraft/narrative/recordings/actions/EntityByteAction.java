package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.LivingEntity;

public class EntityByteAction extends Action {

    private byte entityByte;
    private byte previousEntityByte;

    public EntityByteAction(int waitTick, byte entityByte, byte previousEntityByte) {
        super(waitTick, ActionType.ENTITY_BYTE);
        this.entityByte = entityByte;
        this.previousEntityByte = previousEntityByte;
    }

    @Override
    public void execute(LivingEntity entity) {
        SynchedEntityData entityData = entity.getEntityData();
        EntityDataAccessor<Byte> ENTITY_BYTE_MASK = new EntityDataAccessor<>(0, EntityDataSerializers.BYTE);
        entityData.set(ENTITY_BYTE_MASK, entityByte);
    }

    @Override
    public void rewind(LivingEntity entity) {
        SynchedEntityData entityData = entity.getEntityData();
        EntityDataAccessor<Byte> ENTITY_BYTE_MASK = new EntityDataAccessor<>(0, EntityDataSerializers.BYTE);
        entityData.set(ENTITY_BYTE_MASK, previousEntityByte);
    }
}
