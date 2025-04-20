package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutscenePlayback;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeCoordinate;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    public void getZoomLevel(CallbackInfoReturnable<Float> callbackInfo) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) return;

        PlayerSession playerSession = Utils.getSessionOrNull(localPlayer.getUUID());
        if (playerSession == null) return;

        cutsceneControllerFov(playerSession, callbackInfo);
        cutscenePlayingFov(playerSession, callbackInfo);

    }

    private void cutsceneControllerFov(PlayerSession playerSession, CallbackInfoReturnable<Float> callbackInfo) {
        CutsceneController cutsceneController = playerSession.getCutsceneController();
        if (cutsceneController == null) return;

        Keyframe keyframePreview = cutsceneController.getCurrentPreviewKeyframe();
        if (keyframePreview == null) return;

        callbackInfo.setReturnValue((float) keyframePreview.getKeyframeCoordinate().getFov());
    }

    private void cutscenePlayingFov(PlayerSession playerSession, CallbackInfoReturnable<Float> callbackInfo) {
        CutscenePlayback cutscenePlayback = playerSession.getCutscenePlayback();
        if(cutscenePlayback == null) return;

        KeyframeCoordinate currentLoc = cutscenePlayback.getCurrentLoc();
        callbackInfo.setReturnValue((float) currentLoc.getFov());
    }
}
