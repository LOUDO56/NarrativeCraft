package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class BreakBlockAction extends Action {

    private int x, y, z;
    private boolean drop;
    private String data;

    public BreakBlockAction(int tick, ActionType actionType, int x, int y, int z, boolean drop, String data) {
        super(tick, actionType);
        this.x = x;
        this.y = y;
        this.z = z;
        this.drop = drop;
        this.data = data;
    }

    public void execute(ServerLevel serverLevel) {
        serverLevel.destroyBlock(new BlockPos(x, y, z), drop);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public String getData() {
        return data;
    }
}
