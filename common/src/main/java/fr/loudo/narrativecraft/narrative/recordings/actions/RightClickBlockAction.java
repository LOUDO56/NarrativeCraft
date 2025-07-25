package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class RightClickBlockAction extends Action {

    private int x, y, z;
    private String directionName;
    private String handName;
    private boolean inside;

    public RightClickBlockAction(int tick, int x, int y, int z, String directionName, String handName, boolean inside) {
        super(tick, ActionType.RIGHT_CLICK_BLOCK);
        this.x = x;
        this.y = y;
        this.z = z;
        this.directionName = directionName;
        this.handName = handName;
        this.inside = inside;
    }

    public void execute(Playback.PlaybackData playbackData) {

        ServerPlayer player = Utils.getServerPlayerByUUID(playbackData.getEntity().getUUID());
        if(player == null) return;

        BlockPos blockPos = new BlockPos(x, y, z);

        ItemStack itemStack = player.getItemInHand(InteractionHand.valueOf(handName));
        if(itemStack.getItem() instanceof SpawnEggItem
                || itemStack.getItem() instanceof BoatItem
                || itemStack.getItem() instanceof BlockItem
        ) return;
        itemStack.setCount(2);
        BlockState blockState = player.serverLevel().getBlockState(blockPos);
        BlockHitResult blockHitResult = new BlockHitResult(
                new Vec3(x, y, z),
                Direction.valueOf(directionName),
                blockPos,
                inside
        );
        UseOnContext useOnContext = new UseOnContext(
                player,
                InteractionHand.valueOf(handName),
                blockHitResult
        );
        InteractionResult result = itemStack.getItem().useOn(useOnContext);
        if(!result.consumesAction()) {
            blockState.useWithoutItem(player.serverLevel(), player, blockHitResult);
        }

    }

    @Override
    public void rewind(Playback.PlaybackData playbackData) {}
}
