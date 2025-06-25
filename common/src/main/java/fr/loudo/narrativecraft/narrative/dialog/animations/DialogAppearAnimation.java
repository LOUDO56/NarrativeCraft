package fr.loudo.narrativecraft.narrative.dialog.animations;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.narrative.dialog.Dialog;
import fr.loudo.narrativecraft.utils.Easing;
import fr.loudo.narrativecraft.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public class DialogAppearAnimation {

    private final long APPEAR_TIME = 200L;

    private final Dialog dialog;
    private long startTime, pauseStartTime;
    private boolean isPaused;
    private double t;
    private Easing easing = Easing.SMOOTH;

    public DialogAppearAnimation(Dialog dialog) {
        this.dialog = dialog;
        t = 0;
        startTime = System.currentTimeMillis();
        isPaused = false;
    }

    public void render(PoseStack poseStack, Minecraft minecraft, AppearType appearType) {
        long currentTime = System.currentTimeMillis();

        if(minecraft.isPaused() && !isPaused) {
            isPaused = true;
            pauseStartTime = currentTime;
        } else if(!minecraft.isPaused() && isPaused) {
            isPaused = false;
            startTime += currentTime - pauseStartTime;
        }

        Vec3 startPosition = dialog.getEntityPosition();
        Vec3 endPosition = dialog.getDialogPosition();
        double x = 0;
        double y = 0;
        double z = 0;
        float scale = 0;
        float opacity = 0;
        if(appearType == AppearType.APPEAR) {
            x = MathUtils.lerp(startPosition.x, endPosition.x, t);
            y = MathUtils.lerp(startPosition.y, endPosition.y, t);
            z = MathUtils.lerp(startPosition.z, endPosition.z, t);
            scale = (float) MathUtils.lerp(0, dialog.getScale(), t);
            opacity = (float) Math.max(0,  MathUtils.lerp(0, 1, t));
        } else if(appearType == AppearType.DISAPPEAR) {
            x = MathUtils.lerp(endPosition.x, startPosition.x, t);
            y = MathUtils.lerp(endPosition.y, startPosition.y, t);
            z = MathUtils.lerp(endPosition.z, startPosition.z, t);
            scale = (float) MathUtils.lerp(dialog.getScale(), 0, t);
            opacity = (float) Math.max(0,  MathUtils.lerp(1, 0, t));
        }
        poseStack.translate(x, y, z);
        poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(scale, -scale, scale);
        int alpha = (int) (opacity * 255.0F);

        int dialogBcColor = dialog.getDialogBackgroundColor();
        dialog.setDialogBackgroundColor(dialogBcColor);

        if(!isPaused) {
            t = Easing.getInterpolation(easing, Math.min((double) (currentTime - startTime) / APPEAR_TIME, 1.0));
        }
    }

    public boolean isAnimating() {
        return t < 1.0;
    }

    public void reset() {
        startTime = System.currentTimeMillis();
        t = 0;
    }

    public void setT(int t) {
        this.t = t;
    }

    public enum AppearType {
        APPEAR,
        DISAPPEAR
    }
}
