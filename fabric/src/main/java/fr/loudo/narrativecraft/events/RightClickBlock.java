package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class RightClickBlock {
    public static InteractionResult onRightClickBlock(Player player, Level level, InteractionHand hand, BlockHitResult blockHitResult) {
        if(!level.isClientSide && NarrativeCraftMod.server != null) {
            ServerPlayer serverPlayer = NarrativeCraftMod.server.getPlayerList().getPlayer(player.getUUID());
            OnRightClickBlock.onRightClick(blockHitResult.getDirection(), blockHitResult.getBlockPos(), hand, blockHitResult.isInside(), serverPlayer);
        }

        return InteractionResult.PASS;
    }
}
