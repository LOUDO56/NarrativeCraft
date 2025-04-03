package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.MovementData;
import net.minecraft.server.level.ServerPlayer;

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
}
