package fr.loudo.narrativecraft.narrative.dialog;

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
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class Dialog {


    private final long DIALOG_TRANSITION_TIME = 500L;
    private final Easing easing = Easing.SMOOTH;
    private final Entity entityServer;
    private Entity entityClient;

    private final DialogAnimationScrollText dialogAnimationScrollText;
    private final DialogAppearAnimation dialogAppearAnimation;
    private final DialogAnimationArrowSkip dialogAnimationArrowSkip;
    private final DialogueTail dialogueTail;
    private final DialogEntityBobbing dialogEntityBobbing;

    private float paddingX, paddingY, scale, interpolatedWidth, interpolatedHeight, oldWidth, oldHeight, oldScale;
    private long startTime, pauseStartTime;
    private boolean acceptNewDialog, unSkippable, dialogEnded, endDialog, isPaused;
    private int dialogBackgroundColor, textDialogColor;
    private Vec2 dialogOffset;
    private double t;

    public Dialog(Entity entityServer, String text, int textColor, int backgroundColor, float paddingX, float paddingY, float scale, float letterSpacing, float gap, int maxWidth) {
        this.paddingX = paddingX;
        this.paddingY = paddingY;
        this.scale = scale * 0.025f;
        this.entityServer = entityServer;
        dialogBackgroundColor = backgroundColor;
        textDialogColor = textColor;
        dialogOffset = new Vec2(1f, -0.5f);
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
        this.entityClient = Minecraft.getInstance().level.getEntity(entityServer.getId());
        if(entityClient == null) return;
        Minecraft minecraft = Minecraft.getInstance();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();

        poseStack.pushPose();

        if(dialogAppearAnimation.isAnimating()) {
            dialogAppearAnimation.render(poseStack, minecraft, acceptNewDialog ? DialogAppearAnimation.AppearType.DISAPPEAR : DialogAppearAnimation.AppearType.APPEAR);
        } else {
            Vec3 dialogPos = getDialogInterpolatedPosition();
            poseStack.translate(dialogPos.x, dialogPos.y, dialogPos.z);
            poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
            float targetScale = scale;
            if(acceptNewDialog && oldScale != scale) {
                targetScale = (float) MathUtils.lerp(oldScale, targetScale, t);
            }
            poseStack.scale(targetScale, -targetScale, targetScale);
            acceptNewDialog = true;
        }

        if (!dialogAppearAnimation.isAnimating() && endDialog && !dialogEnded) {
            dialogEnded = true;
            StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
            if(storyHandler != null) {
                storyHandler.setCurrentDialogBox(null);
                if(!unSkippable) {
                    storyHandler.showDialog();
                }
            }
        }
        DialogOffsetSide dialogOffsetSide = getDialogOffsetSide();

        drawDialogBackground(poseStack, bufferSource, dialogOffsetSide);

        double diffY = getDialogPosition().y - getEntityPosition().y;

        switch (dialogOffsetSide) {
            case RIGHT -> poseStack.translate(getInterpolatedWidth(), diffY < 0 ? getHeight() : 0, 0);
            case LEFT -> poseStack.translate(-getInterpolatedWidth(), diffY < 0 ? getHeight() : 0, 0);
            case DOWN -> poseStack.translate(0, getHeight(), 0);
        }

        dialogueTail.draw(poseStack, bufferSource, minecraft.gameRenderer.getMainCamera());

        if(diffY == 0) {
            switch (dialogOffsetSide) {
                case RIGHT, LEFT -> poseStack.translate(0, getHeight() / 2, 0);
            }
        }

        if(!dialogAppearAnimation.isAnimating() && t >= 1.0) {
            dialogAnimationScrollText.render(poseStack, bufferSource);
        }

        bufferSource.endBatch(RenderType.textBackgroundSeeThrough());
        if(dialogAnimationScrollText.isFinished() && !endDialog) {
            dialogAnimationArrowSkip.render(poseStack, minecraft, bufferSource);
        }

        bufferSource.endBatch();
        poseStack.popPose();
    }

    private void drawDialogBackground(PoseStack poseStack, MultiBufferSource bufferSource, DialogOffsetSide dialogOffsetSide) {

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
            if(!isPaused) {
                if(oldHeight != height || oldWidth != width) {
                    t = Easing.getInterpolation(easing, Math.min(1, (double) (System.currentTimeMillis() - startTime) / DIALOG_TRANSITION_TIME));
                } else {
                    t = 1.0;
                }
            }
            interpolatedWidth = (float) MathUtils.lerp(oldWidth, width, t);
            interpolatedHeight = (float) MathUtils.lerp(oldHeight, height, t);
            width = interpolatedWidth;
            height = interpolatedHeight;
        }

        double diffY = getDialogPosition().y - getEntityPosition().y;

        switch (dialogOffsetSide) {
            case UP -> {
                vertexConsumer.addVertex(matrix4f, -width, 0, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
                vertexConsumer.addVertex(matrix4f, width, 0, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
                vertexConsumer.addVertex(matrix4f, width, -height, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
                vertexConsumer.addVertex(matrix4f, -width, -height, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
            }
            case RIGHT -> {
                if(diffY < 0) {
                    vertexConsumer.addVertex(matrix4f, 0, 0, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer.addVertex(matrix4f, 0, height, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer.addVertex(matrix4f, width * 2, height, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer.addVertex(matrix4f, width * 2, 0, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
                } else if(diffY > 0) {
                    vertexConsumer.addVertex(matrix4f, 0, -height, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer.addVertex(matrix4f, 0, 0, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer.addVertex(matrix4f, width * 2, 0, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer.addVertex(matrix4f, width * 2, -height, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
                } else {
                    vertexConsumer.addVertex(matrix4f, 0, -height / 2, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer.addVertex(matrix4f, 0, height / 2, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer.addVertex(matrix4f, width * 2, height / 2, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer.addVertex(matrix4f, width * 2, -height / 2, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
                }
            }
            case LEFT -> {
                if(diffY < 0) {
                    vertexConsumer.addVertex(matrix4f, -width * 2, 0, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer.addVertex(matrix4f, -width * 2, height, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer.addVertex(matrix4f, 0, height, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer.addVertex(matrix4f, 0, 0, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
                } else if(diffY > 0) {
                    vertexConsumer.addVertex(matrix4f, 0, 0, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer.addVertex(matrix4f, 0, -height, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer.addVertex(matrix4f, -width * 2, -height, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer.addVertex(matrix4f, -width * 2, 0, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
                } else {
                    vertexConsumer.addVertex(matrix4f, 0, height / 2, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer.addVertex(matrix4f, 0, -height / 2, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer.addVertex(matrix4f, -width * 2, -height / 2, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
                    vertexConsumer.addVertex(matrix4f, -width * 2, height / 2, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
                }
            }
            case DOWN -> {
                vertexConsumer.addVertex(matrix4f, -width, height, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
                vertexConsumer.addVertex(matrix4f, width, height, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
                vertexConsumer.addVertex(matrix4f, width, 0, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
                vertexConsumer.addVertex(matrix4f, -width, 0, 0).setColor(dialogBackgroundColor).setLight(LightTexture.FULL_BRIGHT);
            }
        }
    }

    public DialogOffsetSide getDialogOffsetSide() {
        Vec3 entityPos = getEntityPosition();
        Vec3 dialogPos = getDialogPosition();

        double offsetX = 0;
        double offsetY = dialogPos.y - entityPos.y;

        Direction direction = Minecraft.getInstance().player.getDirection();

        if (direction == Direction.EAST) {
            offsetX = dialogPos.z - entityPos.z;
        } else if (direction == Direction.WEST) {
            offsetX = entityPos.z - dialogPos.z;
        } else if (direction == Direction.NORTH) {
            offsetX = dialogPos.x - entityPos.x;
        } else if (direction == Direction.SOUTH) {
            offsetX = entityPos.x - dialogPos.x;
        }


        if(offsetY >= 0 && offsetX >= -0.2 && offsetX <= 0.2) {
            return DialogOffsetSide.UP;
        } else if (offsetY <= 0 && offsetX >= -0.2 && offsetX <= 0.2) {
            return DialogOffsetSide.DOWN;
        } else if (offsetX <= 0.2) {
            return DialogOffsetSide.LEFT;
        } else if (offsetX >= 0.2) {
            return DialogOffsetSide.RIGHT;
        }
        return null;
    }

    public void reset() {
        dialogEnded = false;
        endDialog = false;
        dialogAnimationScrollText.reset();
        dialogAnimationArrowSkip.reset();
        startTime = System.currentTimeMillis();
        if(oldWidth == getWidth() && oldHeight == getHeight() && oldScale == scale) {
            t = 1;
        } else {
            t = 0;
        }
    }

    public Entity getEntityServer() {
        return entityServer;
    }

    public float getPaddingX() {
        return paddingX;
    }

    public float getPaddingY() {
        return paddingY;
    }

    public Vec2 getDialogOffset() {
        return dialogOffset;
    }

    public Vec3 getEntityPosition() {
        Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        return new Vec3(
                entityClient.getX() - camPos.x,
                entityClient.getY() + entityClient.getEyeHeight() - camPos.y,
                entityClient.getZ() - camPos.z
        );
    }

    public Vec3 getDialogPosition() {
        Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        double offsetX = 0;
        double offsetZ = 0;

        switch (Minecraft.getInstance().player.getDirection()) {
            case EAST -> offsetZ = dialogOffset.x;
            case WEST -> offsetZ = -dialogOffset.x;
            case SOUTH -> offsetX = -dialogOffset.x;
            case NORTH -> offsetX = dialogOffset.x;
        }
        return new Vec3(
                entityClient.getX() + offsetX - camPos.x,
                entityClient.getY() + entityClient.getEyeHeight() + dialogOffset.y - camPos.y,
                entityClient.getZ() + offsetZ - camPos.z
        );
    }

    public Vec3 getDialogInterpolatedPosition() {
        Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Vec3 lastPos = new Vec3(entityClient.xOld, entityClient.yOld, entityClient.zOld);
        Vec3 newPos = entityClient.position();
        Vec3 interpolatedPos = Mth.lerp(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true), lastPos, newPos);
        double offsetX = 0;
        double offsetZ = 0;

        switch (Minecraft.getInstance().player.getDirection()) {
            case EAST -> offsetZ = dialogOffset.x;
            case WEST -> offsetZ = -dialogOffset.x;
            case SOUTH -> offsetX = -dialogOffset.x;
            case NORTH -> offsetX = dialogOffset.x;
        }
        return new Vec3(
                interpolatedPos.x + offsetX - camPos.x,
                interpolatedPos.y + entityClient.getEyeHeight() + dialogOffset.y - camPos.y,
                interpolatedPos.z + offsetZ - camPos.z
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

    public void setOldScale(float oldScale) {
        this.oldScale = oldScale * 0.025f;
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

    public boolean isAcceptNewDialog() {
        return acceptNewDialog;
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
        unSkippable = true;
        endDialog();
    }

    public void setDialogOffset(Vec2 dialogOffset) {
        this.dialogOffset = dialogOffset;
    }

    public enum DialogOffsetSide {
        UP,
        LEFT,
        RIGHT,
        DOWN
    }
}
