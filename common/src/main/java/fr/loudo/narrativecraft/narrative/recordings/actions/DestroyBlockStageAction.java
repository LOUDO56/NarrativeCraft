package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public class DestroyBlockStageAction extends Action {

    private int id;
    private int x,y,z;
    private int progress;

    public DestroyBlockStageAction(int tick, ActionType actionType, int id, int x, int y, int z, int progress) {
        super(tick, actionType);
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.progress = progress;
    }

    public void execute(ServerLevel serverLevel) {
        serverLevel.getServer().getPlayerList().broadcastAll(new ClientboundBlockDestructionPacket(id, new BlockPos(x, y, z), progress));
    }

    public void execute(ServerLevel serverLevel, boolean reset) {
        serverLevel.getServer().getPlayerList().broadcastAll(new ClientboundBlockDestructionPacket(id, new BlockPos(x, y, z), reset ? -1 : progress));
    }

    public int getProgress() {
        return progress;
    }

}
