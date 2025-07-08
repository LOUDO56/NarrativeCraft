package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.mixin.fields.EntityFields;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;

public class EntityByteAction extends Action {

    private final byte entityByte;
    private final byte previousEntityByte;

    public EntityByteAction(int waitTick, byte entityByte, byte previousEntityByte) {
        super(waitTick, ActionType.ENTITY_BYTE);
        this.entityByte = entityByte;
        this.previousEntityByte = previousEntityByte;
    }

    @Override
    public void execute(Entity entity) {
        SynchedEntityData entityData = entity.getEntityData();
        entityData.set(EntityFields.getDATA_SHARED_FLAGS_ID(), entityByte);
    }

    @Override
    public void rewind(Entity entity) {
        SynchedEntityData entityData = entity.getEntityData();
        entityData.set(EntityFields.getDATA_SHARED_FLAGS_ID(), previousEntityByte);
    }
}
