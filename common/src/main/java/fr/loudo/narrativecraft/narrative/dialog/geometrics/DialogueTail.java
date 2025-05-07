package fr.loudo.narrativecraft.narrative.dialog.geometrics;

import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;

public class DialogueTail {

    private final float width;
    private final float height;

    public DialogueTail(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public void draw(GuiGraphics guiGraphics, int color, float XPoint, float YPoint, float minX, float maxX, float maxY, float scale) {
        Minecraft client = Minecraft.getInstance();

        float dialogWidth = minX + maxX;
        float rescaledWidth = ScreenUtils.getPixelValue(width, scale);
        float rescaledHeight = ScreenUtils.getPixelValue(height, scale);

        float firstTailPoint = XPoint - rescaledWidth / 2.0F;
        float secondTailPoint = XPoint  + rescaledWidth / 2.0F;
        float endTailY = YPoint + rescaledHeight;

        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        VertexConsumer vertexConsumer = client.renderBuffers().bufferSource().getBuffer(RenderType.gui());
        vertexConsumer.addVertex(matrix4f, firstTailPoint, maxY, 0).setColor(color);
        vertexConsumer.addVertex(matrix4f, XPoint, endTailY, 0).setColor(color);
        vertexConsumer.addVertex(matrix4f, secondTailPoint, maxY, 0).setColor(color);
        vertexConsumer.addVertex(matrix4f, secondTailPoint, maxY, 0).setColor(color);

        client.renderBuffers().bufferSource().endBatch();
    }
}
