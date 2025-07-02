package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.MovementData;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

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
        ServerLevel serverLevel = Utils.getServerLevel();
        List<Action> placeList = actions.stream().filter(action -> action instanceof PlaceBlockAction).toList();
        for (Action action : actions) {
            if (action instanceof PlaceBlockAction) {
                action.rewind(entity);
            }

            if (action instanceof BreakBlockAction breakBlockAction) {
                BlockPos brokenPos = breakBlockAction.getBlockPos();

                boolean hasMatchingPlacedBlock = placeList.stream().anyMatch(action1 ->
                        action1 instanceof PlaceBlockAction &&
                                ((PlaceBlockAction) action1).getBlockPos().equals(brokenPos)
                );

                boolean isStillPresent = !serverLevel
                        .getBlockState(brokenPos)
                        .isAir();

                if (!hasMatchingPlacedBlock && !isStillPresent) {
                    action.rewind(entity);
                }
            }
        }
    }
}
