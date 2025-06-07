package fr.loudo.narrativecraft.narrative.dialog;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.dialog.animations.DialogAnimationArrowSkip;
import fr.loudo.narrativecraft.narrative.dialog.animations.DialogAnimationScrollText;
import fr.loudo.narrativecraft.narrative.dialog.animations.DialogAppearAnimation;
import fr.loudo.narrativecraft.narrative.dialog.animations.DialogEntityBobbing;
import fr.loudo.narrativecraft.narrative.dialog.geometrics.DialogueTail;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.utils.Easing;
import fr.loudo.narrativecraft.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class Dialog {


    private final long DIALOG_TRANSITION_TIME = 500L;
    private final Easing easing = Easing.SMOOTH;
    private final Entity entity;

    private DialogAnimationScrollText dialogAnimationScrollText;
    private DialogAppearAnimation dialogAppearAnimation;
    private DialogAnimationArrowSkip dialogAnimationArrowSkip;
    private DialogueTail dialogueTail;
    private DialogEntityBobbing dialogEntityBobbing;

    private float paddingX, paddingY, scale, interpolatedWidth, interpolatedHeight, oldWidth, oldHeight, oldScale;
    private long startTime, pauseStartTime;
    private boolean acceptNewDialog, unSkippable, dialogEnded, endDialog, isPaused;
    private int dialogBackgroundColor, textDialogColor;
    private Vec3 dialogOffset;
    private double t;

    public Dialog(Entity entity, String text, int textColor, int backgroundColor, float paddingX, float paddingY, float scale, float letterSpacing, float gap, int maxWidth) {
        this.paddingX = paddingX;
        this.paddingY = paddingY;
        this.scale = scale * 0.025f;
        this.entity = entity;
        dialogBackgroundColor = backgroundColor;
        textDialogColor = textColor;
        dialogOffset = new Vec3(0, 0.8, 0);
        dialogAnimationScrollText = new DialogAnimationScrollText(text, letterSpacing, gap, maxWidth, this);
        dialogAppearAnimation = new DialogAppearAnimation(this);
        dialogAnimationArrowSkip = new DialogAnimationArrowSkip(this, 2.5f, 2.5f, 8f, -3f, 400L, 0xFFFFFF, 80, Easing.SMOOTH);
        dialogueTail = new DialogueTail(this, 5f, 10f, 0);
        dialogEntityBobbing = new DialogEntityBobbing(this);
        acceptNewDialog = false;
        oldWidth = getWidth();
        oldHeight = getHeight();
        oldScale = scale;
        t = 1.0;
        isPaused = false;
        unSkippable = false;
        endDialog = false;
        dialogEnded = false;
    }

    public void render(PoseStack poseStack) {
        Minecraft minecraft = Minecraft.getInstance();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();

        poseStack.pushPose();

        if(dialogAppearAnimation.isAnimating()) {
            dialogAppearAnimation.play(poseStack, minecraft, acceptNewDialog ? DialogAppearAnimation.AppearType.DISAPPEAR : DialogAppearAnimation.AppearType.APPEAR);
        } else {
            acceptNewDialog = true;
            Vec3 dialogPos = getDialogPosition();
            poseStack.translate(dialogPos.x, dialogPos.y, dialogPos.z);
            poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
            float targetScale = scale;
            if(acceptNewDialog && t < 1.0) {
                targetScale = (float) MathUtils.lerp(oldScale, targetScale, t);
            }
            poseStack.scale(targetScale, -targetScale, targetScale);
        }

        if (!dialogAppearAnimation.isAnimating() && endDialog && !dialogEnded) {
            dialogEnded = true;
            if(!unSkippable) {
                StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
                if(storyHandler != null) {
                    storyHandler.setCurrentDialogBox(null);
                    storyHandler.showDialog();
                }
            }
        }

        drawDialogBackground(poseStack, bufferSource);
        dialogueTail.draw(poseStack, bufferSource, minecraft.gameRenderer.getMainCamera());


        if(!dialogAppearAnimation.isAnimating() && t >= 1.0) {
            dialogAnimationScrollText.show(poseStack, bufferSource);
        }

        bufferSource.endBatch(RenderType.textBackgroundSeeThrough());
        if(dialogAnimationScrollText.isFinished() && !endDialog) {
            dialogAnimationArrowSkip.render(poseStack, minecraft, bufferSource);
        }

        bufferSource.endBatch();
        poseStack.popPose();
    }

    private void drawDialogBackground(PoseStack poseStack, MultiBufferSource bufferSource) {

        Minecraft minecraft = Minecraft.getInstance();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.textBackgroundSeeThrough());
        Matrix4f matrix4f = poseStack.last().pose();

        if(minecraft.isPaused() && !isPaused) {
            isPaused = true;
            pauseStartTime = System.currentTimeMillis();
        } else if(!minecraft.isPaused() && isPaused) {
            isPaused = false;
            startTime += System.currentTimeMillis() - pauseStartTime;
        }

        float width = getWidth();
        float height = getHeight();
        if(acceptNewDialog && t >= 1.0) {
            oldWidth = width;
            oldHeight = height;
            oldScale = scale;
        } else {
            interpolatedWidth = (float) MathUtils.lerp(oldWidth, width, t);
            interpolatedHeight = (float) MathUtils.lerp(oldHeight, height, t);
            width = interpolatedWidth;
            height = interpolatedHeight;
            if(!isPaused) {
                t = Easing.getInterpolation(easing, Math.min(1, (double) (System.currentTimeMillis() - startTime) / DIALOG_TRANSITION_TIME));
            }
        }

        vertexConsumer.addVertex(matrix4f, -width, 0, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, width, 0, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, width, -height, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, -width, -height, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);

    }

    public void reset() {
        dialogAnimationScrollText.reset();
        dialogAnimationArrowSkip.reset();
        startTime = System.currentTimeMillis();
        if(oldWidth == getWidth() && oldHeight == getHeight() && oldScale == scale) {
            t = 1;
        } else {
            t = 0;
        }
    }

    public Entity getEntity() {
        return entity;
    }

    public float getPaddingX() {
        return paddingX;
    }

    public float getPaddingY() {
        return paddingY;
    }

    public Vec3 getDialogOffset() {
        return dialogOffset;
    }

    public Vec3 getEntityPosition() {
        Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        return new Vec3(
                entity.getX() - camPos.x,
                entity.getY() + entity.getEyeHeight() - camPos.y,
                entity.getZ() - camPos.z
        );
    }

    public Vec3 getDialogPosition() {
        Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        return new Vec3(
                entity.getX() + dialogOffset.x - camPos.x,
                entity.getY() + entity.getEyeHeight() + dialogOffset.y - camPos.y,
                entity.getZ() + dialogOffset.z - camPos.z
        );
    }

    public void setPaddingX(float paddingX) {
        this.paddingX = paddingX;
    }

    public void setPaddingY(float paddingY) {
        this.paddingY = paddingY;
    }

    public void setScale(float scale) {
        this.scale = scale * 0.025f;
    }

    public void setGap(float gap) {
        dialogAnimationScrollText.setGap(gap);
    }

    public void setLetterSpacing(float letterSpacing) {
        dialogAnimationScrollText.setLetterSpacing(letterSpacing);
    }

    public void setText(String text) {
        dialogAnimationScrollText.setText(text);
    }

    public float getScale() {
        return scale;
    }

    public int getDialogBackgroundColor() {
        return dialogBackgroundColor;
    }

    public void setDialogBackgroundColor(int dialogBackgroundColor) {
        this.dialogBackgroundColor = dialogBackgroundColor;
    }

    public DialogAnimationScrollText getDialogAnimationScrollText() {
        return dialogAnimationScrollText;
    }

    public int getTextDialogColor() {
        return textDialogColor;
    }

    public void setTextDialogColor(int textDialogColor) {
        this.textDialogColor = textDialogColor;
    }

    public float getWidth() {
        return dialogAnimationScrollText.getMaxWidthLine() / 2.0F + 2 * paddingX;
    }

    public float getHeight() {
        return dialogAnimationScrollText.getTotalHeight();
    }

    public void setMaxWidth(int maxWidth) {
        dialogAnimationScrollText.setMaxWidth(maxWidth);
    }

    public float getInterpolatedHeight() {
        return interpolatedHeight;
    }

    public boolean isUnSkippable() {
        return unSkippable;
    }

    public float getInterpolatedWidth() {
        return interpolatedWidth;
    }

    public DialogEntityBobbing getDialogEntityBobbing() {
        return dialogEntityBobbing;
    }

    public boolean isAnimating() {
        return t < 1.0 || dialogAppearAnimation.isAnimating();
    }

    public void endDialog() {
        endDialog = true;
        dialogAppearAnimation.reset();
    }

    public void endDialogAndDontSkip() {
        endDialog();
        unSkippable = true;
    }
}
