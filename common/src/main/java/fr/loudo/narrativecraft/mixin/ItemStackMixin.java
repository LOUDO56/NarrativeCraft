package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.items.ModItems;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "use", at = @At(value = "HEAD"))
    private void onRightClickItem(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if(!level.isClientSide) {
            ServerPlayer serverPlayer = NarrativeCraftMod.server.getPlayerList().getPlayer(player.getUUID());
            PlayerSession playerSession = NarrativeCraftMod.getInstance().getPlayerSessionManager().getPlayerSession(serverPlayer);
            if(playerSession != null) {
                CutsceneController cutsceneController = playerSession.getCutsceneController();
                if(cutsceneController != null) {
                    Inventory inventory = serverPlayer.getInventory();
                    ItemStack itemStack = (ItemStack) (Object) this;
                    if(itemStack.getCustomName().getString().equals(ModItems.cutscenePause.getCustomName().getString())) {
                        cutsceneController.resume();
                        serverPlayer.getInventory().setItem(inventory.getSelectedSlot(), ModItems.cutscenePlaying);
                        serverPlayer.sendSystemMessage(Translation.message("cutscene.edit.resume"));
                    } else if(itemStack.getCustomName().getString().equals(ModItems.cutscenePlaying.getCustomName().getString())) {
                        cutsceneController.pause();
                        serverPlayer.getInventory().setItem(inventory.getSelectedSlot(), ModItems.cutscenePause);
                        serverPlayer.sendSystemMessage(Translation.message("cutscene.edit.pause"));
                    }
                }
            }
        }
    }
}
