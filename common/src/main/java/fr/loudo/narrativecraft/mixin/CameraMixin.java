package fr.loudo.narrativecraft.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import fr.loudo.narrativecraft.keys.ModKeys;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.utils.PlayerCoord;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
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

    @Inject(method = "setup", at = @At(value = "HEAD"), cancellable = true)
    private void update(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {

        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if(localPlayer != null) {
            PlayerSession playerSession = Utils.getSessionOrNull(localPlayer.getUUID());
            if(playerSession != null ){
                CutsceneController cutsceneController = playerSession.getCutsceneController();
                if(cutsceneController != null) {
                    Keyframe keyframePreview = cutsceneController.getCurrentPreviewKeyframe();
                    if(keyframePreview != null) {
                        Minecraft client = Minecraft.getInstance();
                        PlayerCoord position = keyframePreview.getPosition();
                        this.setPosition(position.getX(), position.getY(), position.getZ());
                        this.setRotation(position.getYRot(), position.getXRot());
                        this.getMaxZoom(keyframePreview.getFov());
                        client.options.setCameraType(CameraType.FIRST_PERSON);
                        client.options.hideGui = true;
                        if(playerSession.getPlayer().isShiftKeyDown()) {
                            cutsceneController.clearCurrentPreviewKeyframe();
                        }
                        ci.cancel();
                    }
                }
            }
        }

    }

}
