package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public class DestroyBlockStageAction extends Action {

    private int id;
    private int x,y,z;
    private int progress;

    public DestroyBlockStageAction(int tick, int id, int x, int y, int z, int progress) {
        super(tick, ActionType.DESTROY_BLOCK_STAGE);
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.progress = progress;
    }

    @Override
    public void execute(Entity entity) {
        ServerLevel serverLevel = Utils.getServerLevel();
        serverLevel.getServer().getPlayerList().broadcastAll(new ClientboundBlockDestructionPacket(id, new BlockPos(x, y, z), progress));
    }

    @Override
    public void rewind(Entity entity) {
        ServerLevel serverLevel = Utils.getServerLevel();
        serverLevel.getServer().getPlayerList().broadcastAll(new ClientboundBlockDestructionPacket(id, new BlockPos(x, y, z), progress == 1 ? -1 : progress));
    }

    public int getProgress() {
        return progress;
    }

}
