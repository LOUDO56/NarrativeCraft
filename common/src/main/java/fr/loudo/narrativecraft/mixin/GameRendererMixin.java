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
    public void getZoomLevel(CallbackInfoReturnable<Float> callbackInfo) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) return;

        PlayerSession playerSession = NarrativeCraftMod.getInstance().getPlayerSession();

        keyframeControllerFov(playerSession, callbackInfo);
        cutscenePlayingFov(playerSession, callbackInfo);
        storyCurrentCamera(callbackInfo);

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
