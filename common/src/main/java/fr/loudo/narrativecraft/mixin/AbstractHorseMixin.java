package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.RecordingHandler;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionDifferenceListener;
import net.minecraft.client.Minecraft;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractHorse.class)
public class AbstractHorseMixin {

    @Shadow @Final private static EntityDataAccessor<Byte> DATA_ID_FLAGS;

    @Inject(method = "tick", at = @At("RETURN"))
    private void narrativecraft$horseTick(CallbackInfo ci) {
        RecordingHandler recordingHandler = NarrativeCraftMod.getInstance().getRecordingHandler();
        Recording recording = recordingHandler.getRecordingOfPlayer(Minecraft.getInstance().player);
        if(recording != null && recording.isRecording()) {
            AbstractHorse horse = (AbstractHorse) (Object) this;
            Recording.RecordingData recordingData = recording.getRecordingDataFromEntity(horse);
            if(recordingData == null || !horse.level().isClientSide) return;
            ActionDifferenceListener actionDifferenceListener = recordingData.getActionDifferenceListener();
            actionDifferenceListener.abstractHorseEntityByteListener(horse.getEntityData().get(DATA_ID_FLAGS));
        }
    }

}
