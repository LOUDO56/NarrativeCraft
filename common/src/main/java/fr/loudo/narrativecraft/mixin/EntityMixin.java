package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.RecordingHandler;
import fr.loudo.narrativecraft.narrative.recordings.actions.HurtAction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "markHurt", at = @At("HEAD"))
    private void mark(CallbackInfo ci) {

        if((Object) this instanceof ServerPlayer player) {
            RecordingHandler recordingHandler = NarrativeCraftMod.getInstance().getRecordingHandler();
            if(recordingHandler.isPlayerRecording(player)) {
                Recording recording = recordingHandler.getRecordingOfPlayer(player);
                HurtAction hurtAction = new HurtAction(recording.getActionDifference().getTick());
                recording.getActionsData().addAction(hurtAction);
            }
        }

    }

}
