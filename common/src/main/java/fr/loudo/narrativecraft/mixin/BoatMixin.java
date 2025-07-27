package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.RecordingHandler;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionDifferenceListener;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Boat.class)
public abstract class BoatMixin {

    @Shadow protected abstract int getBubbleTime();

    @Shadow public abstract boolean getPaddleState(int p_363453_);

    @Inject(method = "tick", at = @At("RETURN"))
    private void narrativecraft$boatTick(CallbackInfo ci) {
        RecordingHandler recordingHandler = NarrativeCraftMod.getInstance().getRecordingHandler();
        Recording recording = recordingHandler.getRecordingOfPlayer(Minecraft.getInstance().player);
        if(recording != null && recording.isRecording()) {
            Boat boat = (Boat) (Object) this;
            Recording.RecordingData recordingData = recording.getRecordingDataFromEntity(boat);
            if(recordingData == null || !boat.level().isClientSide) return;
            ActionDifferenceListener actionDifferenceListener = recordingData.getActionDifferenceListener();
            actionDifferenceListener.abstractBoatEntityBubbleListener(getBubbleTime());
            actionDifferenceListener.abstractBoatEntityPaddleListener(getPaddleState(0), getPaddleState(1));
        }
    }

}
