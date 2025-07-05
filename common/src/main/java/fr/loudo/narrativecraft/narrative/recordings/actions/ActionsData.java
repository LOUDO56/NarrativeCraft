package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.MovementData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionsData {

    private transient LivingEntity entity;
    private int entityId;
    private int spawnTick;
    private final List<MovementData> movementData;
    private final List<Action> actions;

    public ActionsData(LivingEntity entity, int spawnTick) {
        this.movementData = new ArrayList<>();
        this.actions = new ArrayList<>();
        this.entity = entity;
        entityId = BuiltInRegistries.ENTITY_TYPE.getId(entity.getType());
        this.spawnTick = spawnTick;
    }

    public void addMovement() {
        MovementData currentLoc = new MovementData(
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                entity.getXRot(),
                entity.getYRot(),
                entity.getYHeadRot(),
                entity.onGround()
        );
        movementData.add(currentLoc);
    }

    public void addAction(Action action) {
        actions.add(action);
    }

    public List<MovementData> getMovementData() {
        return movementData;
    }

    public List<Action> getActions() {
        return actions;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public void setEntity(LivingEntity entity) {
        this.entity = entity;
    }

    public int getSpawnTick() {
        return spawnTick;
    }

    public int getEntityId() {
        return entityId;
    }

    public void reset(LivingEntity entity) {
        Map<BlockPos, Action> latestActions = new HashMap<>();

        for (Action action : actions) {
            BlockPos pos = getPosFromAction(action);
            if(pos == null) continue;
            latestActions.putIfAbsent(pos, action);
        }

        for (Map.Entry<BlockPos, Action> entry : latestActions.entrySet()) {
            Action action = entry.getValue();

            if (action instanceof PlaceBlockAction place) {
                place.rewind(entity);
            } else if (action instanceof BreakBlockAction breakBlockAction) {
                breakBlockAction.rewind(entity);
            }
        }

    }

    private BlockPos getPosFromAction(Action action) {
        if (action instanceof PlaceBlockAction p) {
            return p.getBlockPos();
        } else if (action instanceof BreakBlockAction b) {
            return b.getBlockPos();
        }
        return null;
    }

}
