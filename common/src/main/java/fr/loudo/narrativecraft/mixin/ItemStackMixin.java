package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.items.ModItems;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

    @Inject(method = "use", at = @At(value = "HEAD"))
    private void onRightClickItem(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if(!level.isClientSide) {
            ServerPlayer serverPlayer = NarrativeCraftMod.server.getPlayerList().getPlayer(player.getUUID());
            PlayerSession playerSession = NarrativeCraftMod.getInstance().getPlayerSessionManager().getPlayerSession(serverPlayer);
            if(playerSession != null) {
                CutsceneController cutsceneController = playerSession.getCutsceneController();
                if(cutsceneController != null) {
                    ItemStack itemStack = (ItemStack) (Object) this;
                    String itemName = itemStack.getCustomName().getString();
                    boolean isControllerItem = false;
                    if(itemName.equals(ModItems.cutscenePause.getCustomName().getString())) {
                        cutsceneController.resume();
                        isControllerItem = true;
                    } else if(itemName.equals(ModItems.cutscenePlaying.getCustomName().getString())) {
                        cutsceneController.pause();
                        isControllerItem = true;
                    } else if (itemName.equals(ModItems.nextSecond.getCustomName().getString())) {
                        cutsceneController.nextSecondSkip();
                        isControllerItem = true;
                    } else if (itemName.equals(ModItems.previousSecond.getCustomName().getString())) {
                        cutsceneController.previousSecondSkip();
                        isControllerItem = true;
                    }

                    if(isControllerItem) {
                        player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.MASTER, 0.5F, 2);
                    }
                }
            }
        }
    }
}
