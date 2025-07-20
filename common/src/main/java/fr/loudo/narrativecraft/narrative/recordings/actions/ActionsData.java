package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.MovementData;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionsData {

    private transient Entity entity;
    private int entityIdRecording;
    private int entityId;
    private int spawnTick;
    private String nbtData;
    private final List<MovementData> movementData;
    private final List<Action> actions;

    public ActionsData(Entity entity, int spawnTick) {
        this.movementData = new ArrayList<>();
        this.actions = new ArrayList<>();
        this.entity = entity;
        if(!(entity instanceof ServerPlayer)) {
            CompoundTag compoundTag = entity.saveWithoutId(new CompoundTag());
            compoundTag.remove("UUID");
            compoundTag.remove("Pos");
            compoundTag.remove("Motion");
            nbtData = compoundTag.toString();
        }
        entityId = BuiltInRegistries.ENTITY_TYPE.getId(entity.getType());
        this.spawnTick = spawnTick;
        entityIdRecording = -1;
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

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(LivingEntity entity) {
        this.entity = entity;
    }

    public int getSpawnTick() {
        return spawnTick;
    }

    public void setSpawnTick(int spawnTick) {
        this.spawnTick = spawnTick;
    }

    public int getEntityIdRecording() {
        return entityIdRecording;
    }

    public void setEntityIdRecording(int entityIdRecording) {
        this.entityIdRecording = entityIdRecording;
    }

    public int getEntityId() {
        return entityId;
    }

    public String getNbtData() {
        return nbtData;
    }

    public void reset(Entity entity) {
        Playback.PlaybackData playbackData = new Playback.PlaybackData(this, null);
        playbackData.setEntity(entity);
        Map<BlockPos, Action> latestActions = new HashMap<>();

        for (Action action : actions) {
            BlockPos pos = getPosFromAction(action);
            if(pos == null) continue;
            latestActions.putIfAbsent(pos, action);
        }

        for (Map.Entry<BlockPos, Action> entry : latestActions.entrySet()) {
            Action action = entry.getValue();

            if (action instanceof PlaceBlockAction place) {
                place.rewind(playbackData);
            } else if (action instanceof BreakBlockAction breakBlockAction) {
                breakBlockAction.rewind(playbackData);
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
