package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public class BreakBlockAction extends Action {

    private int x, y, z;
    private boolean drop;

    public BreakBlockAction(int tick, ActionType actionType, int x, int y, int z, boolean drop) {
        super(tick, actionType);
        this.x = x;
        this.y = y;
        this.z = z;
        this.drop = drop;
    }

    public void execute(ServerLevel serverLevel) {
        serverLevel.destroyBlock(new BlockPos(x, y, z), drop);
    }
}
