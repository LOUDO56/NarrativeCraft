package fr.loudo.narrativecraft.narrative.dialog.geometrics;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.loudo.narrativecraft.narrative.dialog.Dialog;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

//TODO: fix interpolation tail being out of box
public class DialogueTail {

    private final Dialog dialog;
    private float width, height, offset;

    public DialogueTail(Dialog dialog, float width, float height, float offset) {
        this.dialog = dialog;
        this.width = width;
        this.height = height;
        this.offset = offset;
    }

    public void draw(PoseStack poseStack, MultiBufferSource bufferSource, Camera camera) {

        TailDirection tailDirection = getTailDirection(camera);

        Vec2 tailOffset = getTailOffset(camera);

        poseStack.pushPose();
        float tailOffsetX = 0;
        float tailOffsetY = 0;
        if(tailDirection == TailDirection.TOP || tailDirection == TailDirection.BOTTOM) {
            tailOffsetX = Math.clamp(tailOffset.x, -dialog.getWidth() + width / 2, dialog.getWidth() - width / 2);
        }

        if(tailDirection == TailDirection.RIGHT) {
            if (dialog.isAnimating()) {
                tailOffsetX = dialog.getInterpolatedWidth();
            } else {
                tailOffsetX = dialog.getWidth();
            }
        }

        if(tailDirection == TailDirection.LEFT) {
            if (dialog.isAnimating()) {
                tailOffsetX = -dialog.getInterpolatedWidth();
            } else {
                tailOffsetX = -dialog.getWidth();
            }
        }

        if(tailDirection == TailDirection.RIGHT || tailDirection == TailDirection.LEFT) {
            tailOffsetY = Math.clamp(tailOffset.y, -dialog.getHeight() + width / 2, -width / 2);
            if(tailOffset.y < -dialog.getHeight() + width / 2) {
                tailDirection = TailDirection.valueOf(tailDirection.name() + "_UP_CORNER");
            } else if(tailOffset.y > dialog.getHeight() + width / 2) {
                tailDirection = TailDirection.valueOf(tailDirection.name() + "_DOWN_CORNER");
            }
        }

        poseStack.translate(tailOffsetX, tailOffsetY, 0);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.textBackgroundSeeThrough());
        Matrix4f matrix4f = poseStack.last().pose();

        float topRight = -width / 2 + offset;
        float topLeft = width / 2 + offset;

        switch (tailDirection) {
            case TOP -> drawTailTop(matrix4f, vertexConsumer, topRight, topLeft);
            case BOTTOM -> drawTailBottom(matrix4f, vertexConsumer, topRight, topLeft);
            case RIGHT -> drawTailRight(matrix4f, vertexConsumer);
            case RIGHT_UP_CORNER -> drawTailUpRightCorner(matrix4f, vertexConsumer);
            case RIGHT_DOWN_CORNER -> drawTailDownRightCorner(matrix4f, vertexConsumer);
            case LEFT -> drawTailLeft(matrix4f, vertexConsumer);
            case LEFT_UP_CORNER -> drawTailUpLeftCorner(matrix4f, vertexConsumer);
            case LEFT_DOWN_CORNER -> drawTailDownLeftCorner(matrix4f, vertexConsumer);
        }

        poseStack.popPose();

    }

    Vec2 getTailOffset(Camera camera) {
        Vec3 entityPos = dialog.getEntityPosition();
        Vec3 dialogPos = dialog.getDialogPosition();
        Vec3 toDialog = dialogPos.subtract(entityPos);

        Vector3f leftVec = camera.getLeftVector();
        Vec3 camRight = new Vec3(leftVec.x(), leftVec.y(), leftVec.z());
        Vector3f upVec = camera.getUpVector();
        Vec3 camUp = new Vec3(upVec.x(), upVec.y(), upVec.z());

        return new Vec2((float) (toDialog.dot(camRight) / dialog.getScale() / 0.025f), (float) (toDialog.dot(camUp) / dialog.getScale() / 0.025f));
    }

    TailDirection getTailDirection(Camera camera) {
        Vec3 entityPos = dialog.getEntityPosition();
        Vec3 dialogPos = dialog.getDialogPosition();
        Vec3 delta = entityPos.subtract(dialogPos);

        Vector3f camUp = camera.getUpVector();
        Vector3f camLeft = camera.getLeftVector();

        Vec3 up = new Vec3(camUp.x(), camUp.y(), camUp.z());
        Vec3 left = new Vec3(camLeft.x(), camLeft.y(), camLeft.z());

        double verticalProjection = delta.dot(up);
        double horizontalProjection = delta.dot(left);

        if (Math.abs(verticalProjection) > Math.abs(horizontalProjection)) {
            return verticalProjection > 0 ? TailDirection.TOP : TailDirection.BOTTOM;
        } else {
            return horizontalProjection > 0 ? TailDirection.LEFT : TailDirection.RIGHT;
        }
    }

    enum TailDirection {
        TOP, BOTTOM, LEFT, LEFT_UP_CORNER, LEFT_DOWN_CORNER, RIGHT, RIGHT_UP_CORNER, RIGHT_DOWN_CORNER
    }


    void drawTailTop(Matrix4f matrix4f, VertexConsumer vertexConsumer, float topRight, float topLeft) {
        vertexConsumer.addVertex(matrix4f, 0, -dialog.getHeight() - height, 0).setColor(dialog.getDialogBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, -topRight, -dialog.getHeight(), 0).setColor(dialog.getDialogBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, -topLeft, -dialog.getHeight(), 0).setColor(dialog.getDialogBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, -topRight, -dialog.getHeight(), 0).setColor(dialog.getDialogBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
    }

    void drawTailBottom(Matrix4f matrix4f, VertexConsumer vertexConsumer, float topRight, float topLeft) {
        vertexConsumer.addVertex(matrix4f, -topRight, 0, 0).setColor(dialog.getDialogBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, -topLeft, 0, 0).setColor(dialog.getDialogBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, 0, height, 0).setColor(dialog.getDialogBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, -topRight, 0, 0).setColor(dialog.getDialogBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
    }

    void drawTailLeft(Matrix4f matrix4f, VertexConsumer vertexConsumer) {
        vertexConsumer.addVertex(matrix4f, -height, 0, 0).setColor(dialog.getDialogBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, 0, -width / 2, 0).setColor(dialog.getDialogBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, 0, width / 2, 0).setColor(dialog.getDialogBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, 0, -width / 2, 0).setColor(dialog.getDialogBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
    }

    void drawTailRight(Matrix4f matrix4f, VertexConsumer vertexConsumer) {
        vertexConsumer.addVertex(matrix4f, height, 0, 0).setColor(dialog.getDialogBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, 0, width / 2, 0).setColor(dialog.getDialogBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, 0, -width / 2, 0).setColor(dialog.getDialogBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, 0, width / 2, 0).setColor(dialog.getDialogBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
    }

    void drawTailUpRightCorner(Matrix4f matrix4f, VertexConsumer vertexConsumer) {
        vertexConsumer.addVertex(matrix4f, height / 2, -4, 0).setColor(dialog.getDialogBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, -width,  -width / 2, 0).setColor(dialog.getDialogBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, 0, width / 2, 0).setColor(dialog.getDialogBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, height / 2, -4, 0).setColor(dialog.getDialogBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
    }

    void drawTailDownRightCorner(Matrix4f matrix4f, VertexConsumer vertexConsumer) {
        vertexConsumer.addVertex(matrix4f, height / 2, 4, 0).setColor(dialog.getDialogBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, 0,  -width / 2, 0).setColor(dialog.getDialogBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, -width, width / 2, 0).setColor(dialog.getDialogBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, height / 2, 4, 0).setColor(dialog.getDialogBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
    }

    void drawTailUpLeftCorner(Matrix4f matrix4f, VertexConsumer vertexConsumer) {
        vertexConsumer.addVertex(matrix4f, -height / 2, -4, 0).setColor(dialog.getDialogBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, 0,  width / 2, 0).setColor(dialog.getDialogBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, width, -width / 2, 0).setColor(dialog.getDialogBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, -height / 2, -4, 0).setColor(dialog.getDialogBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
    }

    void drawTailDownLeftCorner(Matrix4f matrix4f, VertexConsumer vertexConsumer) {
        vertexConsumer.addVertex(matrix4f, -height / 2, 4, 0).setColor(dialog.getDialogBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, width,  width / 2, 0).setColor(dialog.getDialogBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, width, -width * 2, 0).setColor(dialog.getDialogBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer.addVertex(matrix4f, -height / 2, 4, 0).setColor(dialog.getDialogBackgroundColor()).setLight(LightTexture.FULL_BRIGHT);
    }



}
