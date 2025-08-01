package fr.loudo.narrativecraft.narrative.dialog.animations;

import fr.loudo.narrativecraft.narrative.dialog.Dialog2d;
import fr.loudo.narrativecraft.utils.Easing;
import fr.loudo.narrativecraft.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ARGB;
import org.joml.Matrix3x2fStack;

public class DialogAppearAnimation2d {

    private final long APPEAR_TIME = 400L;

    private final Dialog2d dialog2d;
    private long startTime, pauseStartTime;
    private boolean isPaused;
    private double t;
    private Easing easing = Easing.SMOOTH;

    public DialogAppearAnimation2d(Dialog2d dialog2d) {
        this.dialog2d = dialog2d;
        t = 0;
        startTime = System.currentTimeMillis();
        isPaused = false;
    }

    public void render(Matrix3x2fStack poseStack, Minecraft minecraft, AppearType appearType) {
        if(!isAnimating()) return;
        long currentTime = System.currentTimeMillis();

        if(minecraft.isPaused() && !isPaused) {
            isPaused = true;
            pauseStartTime = currentTime;
        } else if(!minecraft.isPaused() && isPaused) {
            isPaused = false;
            startTime += currentTime - pauseStartTime;
        }

        float scale = 0;
        float opacity = 0;
        if(appearType == AppearType.APPEAR) {
            scale = (float) MathUtils.lerp(0.75f, 1, t);
            opacity = (float) Math.max(0,  MathUtils.lerp(0, 1, t));
        } else if(appearType == AppearType.DISAPPEAR) {
            scale = (float) MathUtils.lerp(1, 0.75f, t);
            opacity = (float) Math.max(0,  MathUtils.lerp(1, 0, t));
        }
        poseStack.scale(scale, scale);
        int alpha = (int) (opacity * 255.0F);

        int dialogBcColor = dialog2d.getBackgroundColor();
        dialog2d.setBackgroundColor(ARGB.color(alpha, dialogBcColor));

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
