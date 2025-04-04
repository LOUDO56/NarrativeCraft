package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.events.OnPlaceBlock;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    @Inject(method = "placeBlock", at = @At(value = "HEAD"))
    private void atPlaceBlock(BlockPlaceContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (!context.getLevel().isClientSide) {
            ServerPlayer serverPlayer = NarrativeCraftMod.server.getPlayerList().getPlayer(context.getPlayer().getUUID());
            OnPlaceBlock.placeBlock(state, context.getClickedPos(), serverPlayer);
        }
    }

}
