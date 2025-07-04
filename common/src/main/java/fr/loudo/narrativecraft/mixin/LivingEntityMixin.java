package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.RecordingHandler;
import fr.loudo.narrativecraft.narrative.recordings.actions.SleepAction;
import fr.loudo.narrativecraft.narrative.recordings.actions.SwingAction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Shadow public InteractionHand swingingArm;

    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;)V", at = @At(value = "HEAD"))
    private void onSwing(InteractionHand hand, CallbackInfo ci) {
        if((Object) this instanceof ServerPlayer player) {
            RecordingHandler recordingHandler = NarrativeCraftMod.getInstance().getRecordingHandler();
            if(recordingHandler.isPlayerRecording(player)) {
                Recording recording = recordingHandler.getRecordingOfPlayer(player);
                SwingAction action = new SwingAction(recording.getActionDifference().getTick(), hand);
                recording.getActionsData().addAction(action);
            }
        }
    }

    @Inject(method = "startSleeping", at = @At("HEAD"))
    private void narrativecraft$startSleep(BlockPos pos, CallbackInfo ci) {
        if((Object) this instanceof ServerPlayer player) {
            RecordingHandler recordingHandler = NarrativeCraftMod.getInstance().getRecordingHandler();
            Recording recording = recordingHandler.getRecordingOfPlayer(player);
            if(recording != null) {
                SleepAction sleepAction = new SleepAction(recording.getTick(), pos);
                recording.getActionsData().addAction(sleepAction);
            }
        }
    }

}
