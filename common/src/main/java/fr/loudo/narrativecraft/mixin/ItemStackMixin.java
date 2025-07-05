package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.actions.UseItemAction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "use", at = @At("HEAD"))
    private void narrativecraft$useItem(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if(level.isClientSide) {
            Recording recording = NarrativeCraftMod.getInstance().getRecordingHandler().getRecordingOfPlayer(player);
            if(recording != null) {
                UseItemAction useItemAction = new UseItemAction(recording.getTick(), hand);
                recording.getActionDataFromEntity(player).addAction(useItemAction);
            }
        }
    }

}
