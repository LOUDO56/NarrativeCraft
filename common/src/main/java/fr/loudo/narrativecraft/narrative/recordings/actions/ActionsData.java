package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.MovementData;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionsData {

    private List<MovementData> movementData;
    private List<Action> actions;

    public ActionsData() {
        this.movementData = new ArrayList<>();
        this.actions = new ArrayList<>();
    }

    public void addMovement(ServerPlayer player) {
        MovementData currentLoc = new MovementData(
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getXRot(),
                player.getYRot(),
                player.getYHeadRot(),
                player.onGround()
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
