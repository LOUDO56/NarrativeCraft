package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.keys.ModKeys;
import fr.loudo.narrativecraft.narrative.chapter.scenes.KeyframeControllerBase;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutscenePlayback;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeCoordinate;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow protected abstract void setPosition(double x, double y, double z);

    @Shadow protected abstract void setRotation(float yRot, float xRot);

    @Shadow protected abstract float getMaxZoom(float maxZoom);

    @Shadow @Final private Quaternionf rotation;

    @Inject(method = "setup", at = @At(value = "HEAD"), cancellable = true)
    private void update(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {

        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) return;

        PlayerSession playerSession = Utils.getSessionOrNull(localPlayer.getUUID());
        if (playerSession == null) return;

        keyframePreviewAction(playerSession, ci);
        cutscenePlaying(playerSession, ci);
        storyCurrentCamera(ci);

    }

    private void keyframePreviewAction(PlayerSession playerSession, CallbackInfo ci) {

        KeyframeControllerBase keyframeControllerBase = playerSession.getKeyframeControllerBase();
        if (keyframeControllerBase == null) return;

        Keyframe keyframePreview = keyframeControllerBase.getCurrentPreviewKeyframe();
        if (keyframePreview == null) return;

        Minecraft client = Minecraft.getInstance();
        KeyframeCoordinate position = keyframePreview.getKeyframeCoordinate();

        this.setPosition(position.getX(), position.getY(), position.getZ());
        this.setRotation(position.getYRot(), position.getXRot());
        this.rotation.rotateZ(-(float) Math.toRadians(position.getZRot()));

        client.options.setCameraType(CameraType.FIRST_PERSON);

        if (ModKeys.SCREEN_KEYFRAME_OPTION.isDown()) {
            keyframePreview.openScreenOption(playerSession.getPlayer());
        }

        ci.cancel();
    }

    private void cutscenePlaying(PlayerSession playerSession, CallbackInfo ci) {
        CutscenePlayback cutscenePlayback = playerSession.getCutscenePlayback();
        if(cutscenePlayback == null) return;

        KeyframeCoordinate position = cutscenePlayback.next();

        this.setPosition(position.getX(), position.getY(), position.getZ());
        this.setRotation(position.getYRot(), position.getXRot());
        this.rotation.rotateZ(-(float) Math.toRadians(position.getZRot()));

        ci.cancel();
    }

    private void storyCurrentCamera(CallbackInfo ci) {
        StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
        if(storyHandler == null) return;

        KeyframeCoordinate position = storyHandler.getCurrentKeyframeCoordinate();

        if(position != null) {
            this.setPosition(position.getX(), position.getY(), position.getZ());
            this.setRotation(position.getYRot(), position.getXRot());
            this.rotation.rotateZ(-(float) Math.toRadians(position.getZRot()));

            ci.cancel();
        }

    }
}
