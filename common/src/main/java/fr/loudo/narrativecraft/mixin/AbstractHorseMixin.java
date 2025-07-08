package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.mixin.fields.AbstractHorseFields;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.RecordingHandler;
import fr.loudo.narrativecraft.narrative.recordings.actions.AbstractHorseByteAction;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractHorse.class)
public class AbstractHorseMixin {

    @Inject(method = "setIsJumping", at = @At("HEAD"))
    private void narrativecraft$isJumping(boolean jumping, CallbackInfo ci) {
        RecordingHandler recordingHandler = NarrativeCraftMod.getInstance().getRecordingHandler();
        Recording recording = recordingHandler.getRecordingOfPlayer(Minecraft.getInstance().player);
        if(recording != null && recording.isRecording()) {
            AbstractHorse horse = (AbstractHorse) (Object) this;
            if(jumping) {
                AbstractHorseByteAction action = new AbstractHorseByteAction(
                        recording.getTick(),
                        (byte) AbstractHorseFields.getFLAG_STANDING(),
                        horse.getEntityData().get(AbstractHorseFields.getDATA_ID_FLAGS())
                );
                recording.getActionDataFromEntity(horse).addAction(action);
            } else {
                if(horse.getEntityData().get(AbstractHorseFields.getDATA_ID_FLAGS()) >= 32) {
                    AbstractHorseByteAction action = new AbstractHorseByteAction(
                            recording.getTick(),
                            (byte) 0,
                            horse.getEntityData().get(AbstractHorseFields.getDATA_ID_FLAGS())
                    );
                    recording.getActionDataFromEntity(horse).addAction(action);
                }
            }
        }
    }

}
