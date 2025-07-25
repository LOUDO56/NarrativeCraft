package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.actions.ActionsData;
import fr.loudo.narrativecraft.narrative.recordings.actions.ItemPickUpAction;
import fr.loudo.narrativecraft.narrative.recordings.actions.RidingAction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class ServerPlayerCommonMixin {

    @Inject(method = "startRiding", at = @At(value = "HEAD"))
    private void narrativecraft$rideEntity(Entity entity, boolean force, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayer serverPlayer = (ServerPlayer) (Object) this;
        Recording recording = NarrativeCraftMod.getInstance().getRecordingHandler().getRecordingOfPlayer(serverPlayer);
        if(recording != null && recording.isRecording()) {
            ActionsData vehicleActionsData = recording.getActionDataFromEntity(entity);
            RidingAction ridingAction = new RidingAction(recording.getTick(), vehicleActionsData.getEntityIdRecording());
            recording.getActionDataFromEntity(serverPlayer).getActions().add(ridingAction);
        }
    }

    @Inject(method = "onItemPickup", at = @At(value = "HEAD"))
    private void narrativecraft$itemPickUp(ItemEntity itemEntity, CallbackInfo ci) {
        ServerPlayer serverPlayer = (ServerPlayer) (Object) this;
        Recording recording = NarrativeCraftMod.getInstance().getRecordingHandler().getRecordingOfPlayer(serverPlayer);
        if(recording != null && recording.isRecording()) {
            ItemPickUpAction action = new ItemPickUpAction(recording.getTick(), recording.getActionDataFromEntity(itemEntity).getEntityIdRecording());
            recording.getActionDataFromEntity(serverPlayer).addAction(action);
        }
    }
}
