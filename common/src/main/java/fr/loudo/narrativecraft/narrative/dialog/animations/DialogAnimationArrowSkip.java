package fr.loudo.narrativecraft.narrative.dialog.animations;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.loudo.narrativecraft.narrative.dialog.Dialog;
import fr.loudo.narrativecraft.narrative.dialog.Dialog2d;
import fr.loudo.narrativecraft.utils.Easing;
import fr.loudo.narrativecraft.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;

public class DialogAnimationArrowSkip {

    private Dialog dialog;
    private Dialog2d dialog2d;
    private Easing easing;
    private float width;
    private float height;
    private float offsetX;
    private float translateXStart;
    private long totalTime;
    private int color;
    private int opacity;
    private long startTime, pauseStartTime;
    private double t;
    private boolean isPaused;

    public DialogAnimationArrowSkip(Dialog dialog, float width, float height, float offsetX, float translateXStart, long totalTime, int color, int opacity, Easing easing) {
        this.dialog = dialog;
        initCommon(width, height, offsetX, translateXStart, totalTime, color, opacity, easing);
    }

    public DialogAnimationArrowSkip(Dialog2d dialog2d, float width, float height, float offsetX, float translateXStart, long totalTime, int color, int opacity, Easing easing) {
        this.dialog2d = dialog2d;
        initCommon(width, height, offsetX, translateXStart, totalTime, color, opacity, easing);
    }

    private void initCommon(float width, float height, float offsetX, float translateXStart, long totalTime, int color, int opacity, Easing easing) {
        this.width = width;
        this.height = height;
        this.offsetX = offsetX;
        this.translateXStart = translateXStart;
        this.totalTime = totalTime;
        this.color = color;
        this.opacity = (opacity * 255) / 100;
        this.easing = easing;
        startTime = 0;
        t = 0;
    }

    public void reset() {
        startTime = 0;
        t = 0;
    }

    public void render(PoseStack poseStack, Minecraft minecraft, MultiBufferSource bufferSource) {
        poseStack.pushPose();
        long now = System.currentTimeMillis();
        if(startTime == 0) startTime = now;
        if (minecraft.isPaused() && !isPaused) {
            isPaused = true;
            pauseStartTime = now;
        } else if (!minecraft.isPaused() && isPaused) {
            isPaused = false;
            startTime += now - pauseStartTime;
        }
        int newOpacity = opacity;
        if(t < 1.0) {
            float newTranslateX = (float) MathUtils.lerp(translateXStart, 0, t);
            poseStack.translate(
                    newTranslateX,
                    0,
                    0
            );
            newOpacity = (int) MathUtils.lerp(0, opacity, t);
            t = Easing.getInterpolation(easing, Math.min((double) (now - startTime) / totalTime, 1.0));
        } else {
            startTime = 0;
        }
        int newColor = newOpacity << 24 | color;
        Matrix4f matrix4f = poseStack.last().pose();

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.textBackgroundSeeThrough());

        float dialogWidth;
        if(dialog != null) {
            dialogWidth = dialog.getWidth();
        } else {
            dialogWidth = dialog2d.getWidth();
        }

        vertexConsumer.addVertex(matrix4f, dialogWidth - width - offsetX, -height, 0.01f).setColor(newColor).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, dialogWidth - width - offsetX, height, 0.01f).setColor(newColor).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, dialogWidth + width - offsetX, 0, 0.01f).setColor(newColor).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, dialogWidth - width - offsetX, -height, 0.01f).setColor(newColor).setLight(LightTexture.FULL_BRIGHT);

        poseStack.popPose();

    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public Easing getEasing() {
        return easing;
    }

    public void setEasing(Easing easing) {
        this.easing = easing;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }

    public float getTranslateXStart() {
        return translateXStart;
    }

    public void setTranslateXStart(float translateXStart) {
        this.translateXStart = translateXStart;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
