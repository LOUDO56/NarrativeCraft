package fr.loudo.narrativecraft.narrative.dialog.animations;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.gui.ICustomGuiRender;
import fr.loudo.narrativecraft.narrative.dialog.Dialog;
import fr.loudo.narrativecraft.narrative.dialog.Dialog2d;
import fr.loudo.narrativecraft.utils.Easing;
import fr.loudo.narrativecraft.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.ARGB;
import org.joml.Matrix3x2fStack;
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

    public void render(PoseStack poseStack, Minecraft minecraft, MultiBufferSource.BufferSource bufferSource) {
        poseStack.pushPose();
        updateAnimationState(minecraft);

        float translatedX = (float) MathUtils.lerp(translateXStart, 0, t);
        int newOpacity = (int) MathUtils.lerp(0, opacity, t);
        int newColor = ARGB.color(t < 1.0 ? newOpacity : opacity, color);

        if (t < 1.0) {
            poseStack.translate(translatedX, 0, 0);
        }

        VertexConsumer vertexConsumer = bufferSource.getBuffer(NarrativeCraftMod.dialogBackgroundRenderType);
        Matrix4f matrix4f = poseStack.last().pose();

        float dialogWidth = getDialogWidth();

        drawQuad(vertexConsumer, matrix4f, dialogWidth, newColor);

        bufferSource.endBatch();
        poseStack.popPose();
    }

    public void render(GuiGraphics guiGraphics, Minecraft minecraft) {
        Matrix3x2fStack poseStack = guiGraphics.pose();
        poseStack.pushMatrix();
        updateAnimationState(minecraft);

        float translatedX = (float) MathUtils.lerp(translateXStart, 0, t);
        int newOpacity = (int) MathUtils.lerp(0, opacity, t);
        int newColor = ARGB.color(t < 1.0 ? newOpacity : opacity, color);

        if (t < 1.0) {
            poseStack.translate(translatedX, 0);
        }

        float dialogWidth = getDialogWidth();

        ((ICustomGuiRender) guiGraphics).drawnDialogSkip(
                dialogWidth,
                width,
                height,
                offsetX,
                newColor
        );

        poseStack.popMatrix();
    }

    private void updateAnimationState(Minecraft minecraft) {
        long now = System.currentTimeMillis();
        if (startTime == 0) startTime = now;

        if (minecraft.isPaused()) {
            if (!isPaused) {
                isPaused = true;
                pauseStartTime = now;
            }
        } else if (isPaused) {
            isPaused = false;
            startTime += now - pauseStartTime;
        }

        if (!isPaused && t < 1.0) {
            double progress = (double) (now - startTime) / totalTime;
            t = Easing.getInterpolation(easing, Math.min(progress, 1.0));
        }

        if (t >= 1.0) {
            startTime = 0;
        }
    }

    private float getDialogWidth() {
        return dialog != null ? dialog.getWidth() : dialog2d.getWidth();
    }

    private void drawQuad(VertexConsumer consumer, Matrix4f matrix, float dialogWidth, int color) {
        float xStart = dialogWidth - width - offsetX;
        float xEnd = dialogWidth + width - offsetX;

        consumer.addVertex(matrix, xStart, -height, 0.01f).setColor(color).setLight(LightTexture.FULL_BRIGHT);
        consumer.addVertex(matrix, xStart, height, 0.01f).setColor(color).setLight(LightTexture.FULL_BRIGHT);
        consumer.addVertex(matrix, xEnd, 0, 0.01f).setColor(color).setLight(LightTexture.FULL_BRIGHT);
        consumer.addVertex(matrix, xStart, -height, 0.01f).setColor(color).setLight(LightTexture.FULL_BRIGHT);
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
