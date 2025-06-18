package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.KeyframeControllerBase;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutscenePlayback;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeCoordinate;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GameRenderer.class, priority = 2000)
public class GameRendererMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    public void getZoomLevel(CallbackInfoReturnable<Float> callbackInfo) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) return;

        PlayerSession playerSession = Utils.getSessionOrNull(localPlayer.getUUID());
        if (playerSession == null) return;

        keyframeControllerFov(playerSession, callbackInfo);
        cutscenePlayingFov(playerSession, callbackInfo);
        storyCurrentCamera(callbackInfo);

    }

//    @Inject(method = "bobView", at = @At("TAIL"))
//    private void applyCameraShake(PoseStack poseStack, float partialTicks, CallbackInfo ci) {
//        double time = System.currentTimeMillis() / 1000.0;
//
//        float intensity = 0.2f;
//        float offsetX = (float) Math.sin(time * 0.4) * intensity;
//        float offsetY = (float) Math.sin(time * 0.5 + 30) * intensity;
//
//        poseStack.translate(offsetX, offsetY, 0);
//    }

    private void keyframeControllerFov(PlayerSession playerSession, CallbackInfoReturnable<Float> callbackInfo) {
        KeyframeControllerBase keyframeControllerBase = playerSession.getKeyframeControllerBase();
        if (keyframeControllerBase == null) return;

        Keyframe keyframePreview = keyframeControllerBase.getCurrentPreviewKeyframe();

        if (keyframePreview == null) return;

        callbackInfo.setReturnValue(keyframePreview.getKeyframeCoordinate().getFov());
    }

    private void cutscenePlayingFov(PlayerSession playerSession, CallbackInfoReturnable<Float> callbackInfo) {
        CutscenePlayback cutscenePlayback = playerSession.getCutscenePlayback();
        if(cutscenePlayback == null) return;

        KeyframeCoordinate currentLoc = cutscenePlayback.getCurrentLoc();
        callbackInfo.setReturnValue((currentLoc.getFov()));
    }

    private void storyCurrentCamera(CallbackInfoReturnable<Float> callbackInfo) {
        StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
        if(storyHandler == null) return;

        KeyframeCoordinate position = storyHandler.getPlayerSession().getSoloCam();

        if(position != null) {
            callbackInfo.setReturnValue(position.getFov());
        }

    }
}
