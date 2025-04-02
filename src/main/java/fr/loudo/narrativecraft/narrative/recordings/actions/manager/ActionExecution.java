package fr.loudo.narrativecraft.narrative.recordings.actions.manager;

import fr.loudo.narrativecraft.narrative.recordings.actions.Action;
import fr.loudo.narrativecraft.narrative.recordings.actions.EntityByteAction;
import fr.loudo.narrativecraft.narrative.recordings.actions.PoseAction;
import fr.loudo.narrativecraft.narrative.recordings.actions.SwingAction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.LivingEntity;

public class ActionExecution {

    public static void execute(LivingEntity entity, Action action) {
        switch (action.getActionType()) {
            case SWING -> {
                SwingAction swingAction = (SwingAction) action;
                entity.swing(swingAction.getInteractionHand());
            }
            case POSE -> {
                PoseAction poseAction = (PoseAction) action;
                entity.setPose(poseAction.getPose());
            }
            case ENTITY_BYTE -> {
                EntityByteAction entityByteAction = (EntityByteAction) action;
                SynchedEntityData entityData = entity.getEntityData();
                EntityDataAccessor<Byte> ENTITY_BYTE_MASK = new EntityDataAccessor<>(0, EntityDataSerializers.BYTE);
                entityData.set(ENTITY_BYTE_MASK, entityByteAction.getEntityByte());
            }
        }
    }

}
