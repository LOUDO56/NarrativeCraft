package fr.loudo.narrativecraft.narrative.dialog.animations;

import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.loudo.narrativecraft.utils.Easing;
import fr.loudo.narrativecraft.utils.MathUtils;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class DialogAnimationArrowSkip {

    private final Easing easing;
    private final float width;
    private final float height;
    private final float offsetX;
    private final float translateXStart;
    private final long totalTime;
    private final int color;
    private final int opacity;
    private long startTime, pauseStartTime;
    private double t;
    private boolean isPaused;

    public DialogAnimationArrowSkip(float width, float height, float offsetX, float translateXStart, long totalTime, int color, int opacity, Easing easing) {
        this.width = width;
        this.height = height;
        this.offsetX = offsetX;
        this.translateXStart = translateXStart;
        this.totalTime = totalTime;
        this.color = color;
        this.opacity = opacity * 255 / 100;
        this.easing = easing;
    }

    public void reset() {
        startTime = System.currentTimeMillis();
        t = 0;
    }

    public void draw(GuiGraphics guiGraphics, float maxX, float maxY, float scale, Vector4f posClip) {
        Minecraft client = Minecraft.getInstance();
        if (posClip.w <= 0) return;
        long now = System.currentTimeMillis();
        if (client.isPaused() && !isPaused) {
            isPaused = true;
            pauseStartTime = now;
        } else if (!client.isPaused() && isPaused) {
            isPaused = false;
            startTime += now - pauseStartTime;
        }
        VertexConsumer vertexConsumer = client.renderBuffers().bufferSource().getBuffer(RenderType.gui());

        float baseX = maxX - ScreenUtils.getPixelValue(width, scale) - ScreenUtils.getPixelValue(offsetX, scale);

        float topLeftY = maxY - ScreenUtils.getPixelValue(height, scale);
        float bottomLeftY = maxY + ScreenUtils.getPixelValue(height, scale);
        float trianglePointX = baseX + ScreenUtils.getPixelValue(width, scale);

        int newOpacity = opacity;
        if (t < 1.0) {
            float newX = (float) MathUtils.lerp(ScreenUtils.getPixelValue(translateXStart, scale), 0, t);
            newOpacity = (int) MathUtils.lerp(0, opacity, t);
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(newX, 0, 0);
            if(!isPaused) {
                t = Easing.getInterpolation(easing, Math.min((double) (System.currentTimeMillis() - startTime) / totalTime, 1.0));
            }
        }
        int newColor = newOpacity << 24 | color;

        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        vertexConsumer.addVertex(matrix4f, baseX, topLeftY, 0)
                .setColor(newColor);
        vertexConsumer.addVertex(matrix4f, baseX, bottomLeftY, 0)
                .setColor(newColor);
        vertexConsumer.addVertex(matrix4f, trianglePointX, maxY, 0)
                .setColor(newColor);
        vertexConsumer.addVertex(matrix4f, trianglePointX, maxY, 0)
                .setColor(newColor);

        if (t < 1.0f) {
            guiGraphics.pose().popPose();
        }

        client.renderBuffers().bufferSource().endBatch();
    }
}
