package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.items.CutsceneEditItems;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.utils.Translation;
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
public abstract class ItemStackMixin {

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
                    boolean playSound = false;
                    if(itemName.equals(CutsceneEditItems.createKeyframeGroup.getCustomName().getString())) {
                        cutsceneController.createKeyframeGroup();
                        playerSession.getPlayer().sendSystemMessage(Translation.message("cutscene.keyframegroup.created", playerSession.getCutsceneController().getSelectedKeyframeGroup().getId()));
                        playSound = true;
                    } else if(itemName.equals(CutsceneEditItems.addKeyframe.getCustomName().getString())) {
                        if(cutsceneController.addKeyframe()) {
                            playerSession.getPlayer().sendSystemMessage(Translation.message("cutscene.keyframe.added", playerSession.getCutsceneController().getSelectedKeyframeGroup().getId()));
                        } else {
                            playerSession.getPlayer().sendSystemMessage(Translation.message("cutscene.keyframe.added.fail"));
                        }
                        playSound = true;
                    } else if(itemName.equals(CutsceneEditItems.cutscenePause.getCustomName().getString())) {
                        cutsceneController.resume();
                        playSound = true;
                    } else if(itemName.equals(CutsceneEditItems.cutscenePlaying.getCustomName().getString())) {
                        cutsceneController.pause();
                        playSound = true;
                    } else if (itemName.equals(CutsceneEditItems.nextSecond.getCustomName().getString())) {
                        cutsceneController.nextSecondSkip();
                        playSound = true;
                    } else if (itemName.equals(CutsceneEditItems.previousSecond.getCustomName().getString())) {
                        cutsceneController.previousSecondSkip();
                        playSound = true;
                    } else if (itemName.equals(CutsceneEditItems.settings.getCustomName().getString())) {
                        cutsceneController.openSettings();
                    }

                    if(playSound) {
                        player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.MASTER, 0.3F, 2);
                    }
                }
            }
        }
    }
}
