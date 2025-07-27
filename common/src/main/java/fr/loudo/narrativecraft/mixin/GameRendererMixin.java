package fr.loudo.narrativecraft.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.KeyframeControllerBase;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutscenePlayback;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeCoordinate;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.inkAction.InkAction;
import fr.loudo.narrativecraft.narrative.story.inkAction.ShakeScreenInkAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    public void getZoomLevel(CallbackInfoReturnable<Double> callbackInfo) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) return;

        PlayerSession playerSession = NarrativeCraftMod.getInstance().getPlayerSession();

        try {
            keyframeControllerFov(playerSession, callbackInfo);
            cutscenePlayingFov(playerSession, callbackInfo);
            storyCurrentCamera(callbackInfo);
        } catch (Exception ignored) {

        }

    }

    @Inject(method = "bobHurt", at = @At(value = "HEAD"))
    private void applyCameraShake(PoseStack poseStack, float partialTicks, CallbackInfo ci) {
        StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
        if(storyHandler != null) {
            for(InkAction inkAction : storyHandler.getInkActionList()) {
                if(inkAction instanceof ShakeScreenInkAction shakeScreenInkAction) {
                    shakeScreenInkAction.shake(poseStack, partialTicks);
                }
            }
        }
    }

    private void keyframeControllerFov(PlayerSession playerSession, CallbackInfoReturnable<Double> callbackInfo) {
        KeyframeControllerBase keyframeControllerBase = playerSession.getKeyframeControllerBase();
        if (keyframeControllerBase == null) return;

        Keyframe keyframePreview = keyframeControllerBase.getCurrentPreviewKeyframe();

        if (keyframePreview == null) return;

        callbackInfo.setReturnValue((double)keyframePreview.getKeyframeCoordinate().getFov());
    }

    private void cutscenePlayingFov(PlayerSession playerSession, CallbackInfoReturnable<Double> callbackInfo) {
        CutscenePlayback cutscenePlayback = playerSession.getCutscenePlayback();
        if(cutscenePlayback == null) return;

        KeyframeCoordinate currentLoc = cutscenePlayback.getCurrentLoc();
        if(currentLoc == null) return;
        callbackInfo.setReturnValue((double)(currentLoc.getFov()));
    }

    private void storyCurrentCamera(CallbackInfoReturnable<Double> callbackInfo) {
        StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
        if(storyHandler == null) return;

        KeyframeCoordinate position = storyHandler.getPlayerSession().getSoloCam();

        if(position != null) {
            callbackInfo.setReturnValue((double)position.getFov());
        }

    }
}
