package fr.loudo.narrativecraft.narrative.dialog.animations;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.loudo.narrativecraft.narrative.dialog.Dialog;
import fr.loudo.narrativecraft.utils.Easing;
import fr.loudo.narrativecraft.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;

public class DialogAnimationArrowSkip {

    private final Dialog dialog;
    private final Easing easing;
    private final float width;
    private final float height;
    private final float offsetX;
    private final float translateXStart;
    private final long totalTime;
    private int color;
    private final int opacity;
    private long startTime, pauseStartTime;
    private double t;
    private boolean isPaused;

    public DialogAnimationArrowSkip(Dialog dialog, float width, float height, float offsetX, float translateXStart, long totalTime, int color, int opacity, Easing easing) {
        this.dialog = dialog;
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

        vertexConsumer.addVertex(matrix4f, dialog.getWidth() - width - offsetX, -height, 0.01f).setColor(newColor).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, dialog.getWidth() - width - offsetX, height, 0.01f).setColor(newColor).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, dialog.getWidth() + width - offsetX, 0, 0.01f).setColor(newColor).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, dialog.getWidth() - width - offsetX, -height, 0.01f).setColor(newColor).setLight(LightTexture.FULL_BRIGHT);

        poseStack.popPose();

//        if(t < 1.0) {
//            poseStack.pushPose();
//            poseStack.translate(new Vec3());
//        }
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }


}
