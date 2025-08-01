package fr.loudo.narrativecraft.gui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import org.joml.Matrix3x2f;

public record SkipArrow2dGui(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2f pose, float dialogWidth, float width, float height, float offsetX, int color, ScreenRectangle scissorArea, ScreenRectangle bounds) implements GuiElementRenderState {

    public SkipArrow2dGui(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2f pose, float dialogWidth, float width, float height, float offsetX, int color, ScreenRectangle scissorArea) {
        this(pipeline, textureSetup, pose, dialogWidth, width, height, offsetX, color, scissorArea, new ScreenRectangle(0, 0, 0, 0));
    }

    @Override
    public void buildVertices(VertexConsumer consumer, float z) {
        consumer.addVertexWith2DPose(this.pose(), dialogWidth - width - offsetX, -height, z).setColor(color);
        consumer.addVertexWith2DPose(this.pose(), dialogWidth - width - offsetX, height, z).setColor(color);
        consumer.addVertexWith2DPose(this.pose(), dialogWidth + width - offsetX, 0, z).setColor(color);
        consumer.addVertexWith2DPose(this.pose(), dialogWidth - width - offsetX, -height, z).setColor(color);
    }

}
