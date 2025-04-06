package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class RightClickBlockAction extends Action {

    private int x, y, z;
    private String directionName;
    private String handName;
    private boolean inside;

    public RightClickBlockAction(int tick, ActionType actionType, int x, int y, int z, String directionName, String handName, boolean inside) {
        super(tick, actionType);
        this.x = x;
        this.y = y;
        this.z = z;
        this.directionName = directionName;
        this.handName = handName;
        this.inside = inside;
    }

    public void execute(ServerPlayer player) {

        BlockPos blockPos = new BlockPos(x, y, z);

        ItemStack itemStack = player.getItemInHand(InteractionHand.valueOf(handName));
        BlockState blockState = player.serverLevel().getBlockState(blockPos);
        BlockHitResult blockHitResult = new BlockHitResult(
                new Vec3(x, y, z),
                Direction.valueOf(directionName),
                blockPos,
                inside
        );
        InteractionResult result = blockState.useItemOn(itemStack, player.serverLevel(), player, InteractionHand.valueOf(handName), blockHitResult);
        if(!result.consumesAction()) {
            blockState.useWithoutItem(player.serverLevel(), player, blockHitResult);
        }
    }
}
