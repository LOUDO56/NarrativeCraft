package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.RecordingHandler;
import fr.loudo.narrativecraft.narrative.recordings.actions.DestroyBlockStageAction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @Shadow @Final private MinecraftServer server;

    @Inject(method = "destroyBlockProgress", at = @At(value = "HEAD"))
    private void onDestroyBlockProgress(int breakerId, BlockPos pos, int progress, CallbackInfo ci) {
        for(ServerPlayer player : this.server.getPlayerList().getPlayers()) {
            RecordingHandler recordingHandler = NarrativeCraftMod.getInstance().getRecordingHandler();
            if(recordingHandler.isPlayerRecording(player) && player.getId() == breakerId) {
                Recording recording = recordingHandler.getRecordingOfPlayer(player);
                DestroyBlockStageAction destroyBlockStageAction = new DestroyBlockStageAction(recording.getActionDifference().getTick(), player.getId(), pos.getX(), pos.getY(), pos.getZ(), progress);
                recording.getActionsData().addAction(destroyBlockStageAction);
            }
        }

    }

}
