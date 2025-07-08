package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.mixin.fields.AbstractBoatFields;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.AbstractBoat;

public class AbstractBoatPaddleAction extends Action {

    private final boolean leftPaddle;
    private final boolean rightPaddle;

    private final boolean oldLeftPaddle;
    private final boolean oldRightPaddle;

    public AbstractBoatPaddleAction(int tick, boolean leftPaddle, boolean rightPaddle, boolean oldLeftPaddle, boolean oldRightPaddle) {
        super(tick, ActionType.ABSTRACT_BOAT_PADDLE);
        this.leftPaddle = leftPaddle;
        this.rightPaddle = rightPaddle;
        this.oldLeftPaddle = oldLeftPaddle;
        this.oldRightPaddle = oldRightPaddle;
    }

    @Override
    public void execute(Entity entity) {
        Entity entity1 = entity.level().getEntity(entity.getId());
        if(entity1 instanceof AbstractBoat) {
            entity.getEntityData().set(AbstractBoatFields.getDATA_ID_PADDLE_LEFT(), leftPaddle);
            entity.getEntityData().set(AbstractBoatFields.getDATA_ID_PADDLE_RIGHT(), rightPaddle);
        }

    }

    @Override
    public void rewind(Entity entity) {
        Entity entity1 = entity.level().getEntity(entity.getId());
        if(entity1 instanceof AbstractBoat) {
            entity.getEntityData().set(AbstractBoatFields.getDATA_ID_PADDLE_LEFT(), oldLeftPaddle);
            entity.getEntityData().set(AbstractBoatFields.getDATA_ID_PADDLE_RIGHT(), oldRightPaddle);
        }
    }
}
