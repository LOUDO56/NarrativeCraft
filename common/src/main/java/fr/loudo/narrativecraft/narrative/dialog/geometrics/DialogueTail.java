package fr.loudo.narrativecraft.narrative.dialog.geometrics;

import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;

//TODO: refractor, it works but it's a bit messy
public class DialogueTail {

    private final float width;
    private final float height;

    public DialogueTail(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public void draw(GuiGraphics guiGraphics, int color, float XPoint, float YPoint, float minX, float maxX, float minY, float maxY, float scale) {
        Minecraft client = Minecraft.getInstance();

        float dialogWidth = maxX + minX;
        float dialogHeight = minY + maxY;
        boolean isCenter = XPoint == dialogWidth / 2.0F; // TODO: put range of center, because it twinks a little
        boolean isLeft = XPoint < dialogWidth / 2.0F;
        boolean isRight = XPoint > dialogWidth / 2.0F;

        float rescaledWidth = ScreenUtils.getPixelValue(width, scale);
        float rescaledHeight = ScreenUtils.getPixelValue(height, scale);
        boolean isDown = maxY > YPoint + rescaledHeight;

        if(!isDown) {

            float tailCoordinateX = isCenter ? XPoint : (isRight ? XPoint - rescaledWidth / 2.0F : XPoint + rescaledWidth / 2.0F );
            float tailCoordinateY = maxY;
            float distance = 0;

            if(XPoint > maxX) {
                distance = (XPoint - maxX);
                tailCoordinateX = maxX - rescaledWidth / 2.0F;
                tailCoordinateY = maxY - rescaledWidth / 2.0F;
                if(distance > rescaledHeight){
                    XPoint = XPoint - distance + rescaledHeight;
                }
            } else if (XPoint < minX) {
                distance = (minX - XPoint);
                tailCoordinateX = minX + rescaledWidth / 2.0F;
                maxY -= rescaledWidth / 2.0F;
                if(distance > rescaledHeight){
                    XPoint = XPoint + distance - rescaledHeight;
                }
            }

            float firstTailPoint = tailCoordinateX - rescaledWidth / 2.0F;
            float secondTailPoint = tailCoordinateX + rescaledWidth / 2.0F;
            float endTailY;
            if ((XPoint < minX || XPoint > maxX) && distance > rescaledHeight){
                endTailY = YPoint + (rescaledHeight / 2.0F);
            } else {
                endTailY = YPoint + rescaledHeight;
            }

            Matrix4f matrix4f = guiGraphics.pose().last().pose();
            VertexConsumer vertexConsumer = client.renderBuffers().bufferSource().getBuffer(RenderType.gui());
            vertexConsumer.addVertex(matrix4f, firstTailPoint, maxY, 0).setColor(color);
            vertexConsumer.addVertex(matrix4f, XPoint, endTailY, 0).setColor(color);
            vertexConsumer.addVertex(matrix4f, secondTailPoint, tailCoordinateY, 0).setColor(color);
            vertexConsumer.addVertex(matrix4f, secondTailPoint, tailCoordinateY, 0).setColor(color);

            client.renderBuffers().bufferSource().endBatch();
        } else {

            isRight = minX > XPoint;
            float XFaceValue = isRight ? minX : maxX;
            float endXPoint = isRight ? minX - rescaledHeight : maxX + rescaledHeight;
            float initialPoint = (dialogHeight / 2.0F);
            float firstTailPoint = initialPoint - (rescaledWidth / 2.0F);
            float secondTailPoint = initialPoint + (rescaledWidth / 2.0F);

            Matrix4f matrix4f = guiGraphics.pose().last().pose();
            VertexConsumer vertexConsumer = client.renderBuffers().bufferSource().getBuffer(RenderType.gui());
            vertexConsumer.addVertex(matrix4f, XFaceValue, firstTailPoint, 0).setColor(color);
            if(isRight) {
                vertexConsumer.addVertex(matrix4f, endXPoint, initialPoint, 0).setColor(color);
                vertexConsumer.addVertex(matrix4f, XFaceValue, secondTailPoint, 0).setColor(color);
                vertexConsumer.addVertex(matrix4f, XFaceValue, secondTailPoint, 0).setColor(color);
            } else {
                vertexConsumer.addVertex(matrix4f, XFaceValue, secondTailPoint, 0).setColor(color);
                vertexConsumer.addVertex(matrix4f, endXPoint, initialPoint, 0).setColor(color);
                vertexConsumer.addVertex(matrix4f, endXPoint, initialPoint, 0).setColor(color);
            }

            client.renderBuffers().bufferSource().endBatch();
        }

    }
}
