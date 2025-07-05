package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.RecordingHandler;
import fr.loudo.narrativecraft.narrative.recordings.actions.ActionsData;
import fr.loudo.narrativecraft.narrative.recordings.actions.HurtAction;
import fr.loudo.narrativecraft.narrative.recordings.actions.SleepAction;
import fr.loudo.narrativecraft.narrative.recordings.actions.SwingAction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Shadow public InteractionHand swingingArm;

    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;)V", at = @At(value = "HEAD"))
    private void onSwing(InteractionHand hand, CallbackInfo ci) {
        if((Object) this instanceof ServerPlayer player) {
            RecordingHandler recordingHandler = NarrativeCraftMod.getInstance().getRecordingHandler();
            if(recordingHandler.isPlayerRecording(player)) {
                Recording recording = recordingHandler.getRecordingOfPlayer(player);
                SwingAction action = new SwingAction(recording.getTick(), hand);
                recording.getActionDataFromEntity(player).addAction(action);
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
                recording.getActionDataFromEntity(player).addAction(sleepAction);
            }
        }
    }

    @Inject(method = "hurtServer", at = @At("HEAD"))
    private void narrativecraft$hurtServer(ServerLevel serverLevel, DamageSource damageSource, float damageHit, CallbackInfoReturnable<Boolean> cir) {
        if(damageSource.getEntity() instanceof ServerPlayer player) {
            RecordingHandler recordingHandler = NarrativeCraftMod.getInstance().getRecordingHandler();
            Recording recording = recordingHandler.getRecordingOfPlayer(player);
            if(recording != null) {
                HurtAction hurtAction = new HurtAction(recording.getTick());
                LivingEntity livingEntity = (LivingEntity) (Object) this;
                ActionsData actionsData = recording.getActionDataFromEntity(livingEntity);
                if(actionsData != null) {
                    actionsData.addAction(hurtAction);
                }
            }
        }
    }

}
