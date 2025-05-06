package fr.loudo.narrativecraft.narrative.dialog.animations;

import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.loudo.narrativecraft.utils.Easing;
import fr.loudo.narrativecraft.utils.MathUtils;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;

public class DialogAnimationArrowSkip {

    private final Easing easing = Easing.EASE_OUT;
    private double t;
    private long totalTime, startTime;

    public DialogAnimationArrowSkip(long totalTime) {
        this.totalTime = totalTime;
        this.t = 0;
        this.startTime = System.currentTimeMillis();
    }

    public void reset() {
        startTime = System.currentTimeMillis();
        t = 0;
    }

    public void draw(GuiGraphics guiGraphics, float minX, float maxX, float minY, float maxY, float scale) {
        Minecraft client = Minecraft.getInstance();
        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        VertexConsumer vertexConsumer = client.renderBuffers().bufferSource().getBuffer(RenderType.gui());

        float baseX = maxX - ScreenUtils.getPixelValue(5f, scale);
        float baseY = maxY - ScreenUtils.getPixelValue(5f, scale);

        float topLeftY = baseY - ScreenUtils.getPixelValue(2f, scale);
        float bottomLeftY = baseY + ScreenUtils.getPixelValue(2f, scale);
        float trianglePointX = baseX + ScreenUtils.getPixelValue(4f, scale);

        vertexConsumer.addVertex(matrix4f, baseX, topLeftY, 0)
                .setColor(1f, 1f, 1f, 1f);
        vertexConsumer.addVertex(matrix4f, baseX, bottomLeftY, 0)
                .setColor(1f, 1f, 1f, 1f);
        vertexConsumer.addVertex(matrix4f, trianglePointX, baseY, 0)
                .setColor(1f, 1f, 1f, 1f);
        vertexConsumer.addVertex(matrix4f, trianglePointX, baseY, 0)
                .setColor(1f, 1f, 1f, 1f);

        if (t < 1.0f) {
            float newX = (float) MathUtils.lerp(-50, 0, t);
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(newX, 0, 0);
            t = Easing.getInterpolation(easing, Math.min((double) (System.currentTimeMillis() - startTime) / totalTime, 1.0));
            guiGraphics.pose().popPose();
        }

        client.renderBuffers().bufferSource().endBatch();
    }


    public boolean isFinished() {
        return t == 1.0;
    }
}
