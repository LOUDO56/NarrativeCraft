package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.network.chat.Component;
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
public abstract class ItemStackMixin {

    @Inject(method = "use", at = @At(value = "HEAD"))
    private void onRightClickItem(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if(level.isClientSide) {
            PlayerSession playerSession = Utils.getSessionOrNull(player.getUUID());
            if(playerSession == null) return;

            CutsceneController cutsceneController = playerSession.getCutsceneController();
            if(cutsceneController == null) return;

            ItemStack itemStack = (ItemStack) (Object) this;
            Component itemName = itemStack.getCustomName();

            if(itemName == null) return;

            cutsceneController.handleItemClick(itemName.getString());
        }
    }
}
