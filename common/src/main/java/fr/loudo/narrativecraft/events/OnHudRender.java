package fr.loudo.narrativecraft.events;

import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.loudo.narrativecraft.mixin.fields.GameRendererFields;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

public class OnHudRender {

    public static Vec3 entityPos;
    public static Entity entity;

    public static void hudRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if(entityPos == null) return;
        Minecraft client = Minecraft.getInstance();
        float dynamicFov = ((GameRendererFields)client.gameRenderer).callGetFov(
                client.gameRenderer.getMainCamera(),
                deltaTracker.getGameTimeDeltaPartialTick(true),
                true
        );
        Matrix4f projection = client.gameRenderer.getProjectionMatrix(dynamicFov);
        Matrix4f view = getViewMatrix(client.gameRenderer.getMainCamera());
        Vector4f posWorld = new Vector4f(
                (float) entityPos.x,
                (float) entityPos.y + 0.9f,
                (float) entityPos.z,
                1.0f
        );

        Vector4f posClip = new Vector4f(posWorld);
        view.transform(posClip);
        projection.transform(posClip);

        if (posClip.w <= 0) return;

        float ndcX = posClip.x / posClip.w;
        float ndcY = posClip.y / posClip.w;
        float ndcZ = posClip.z / posClip.w;

        int screenWidth = (client.getWindow().getScreenWidth()) / client.options.guiScale().get();
        int screenHeight = (client.getWindow().getScreenHeight()) / client.options.guiScale().get();

        float baseSize = 2.0f;
        float fovScale = (70.0f / dynamicFov);
        float scale = (100.0f / posClip.w) * fovScale;

        float rectWidth = (baseSize + 10f) * scale;
        float rectHeight = baseSize * scale;

        float screenX = ((ndcX + 1.0f) / 2.0f * screenWidth) - (rectWidth / 2.0f);
        float screenY = ((1.0f - ndcY) / 2.0f * screenHeight) - (rectHeight / 2.0f);

        float minX = screenX;
        float minY = screenY;
        float maxX = screenX + rectWidth;
        float maxY = screenY + rectHeight;
        float z = 0;

        int color = 0xD9000000; // opaque noir

        if (minX > maxX) {
            float i = minX;
            minX = maxX;
            maxX = i;
        }

        if (minY > maxY) {
            float j = minY;
            minY = maxY;
            maxY = j;
        }

        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        VertexConsumer vertexconsumer = client.renderBuffers().bufferSource().getBuffer(RenderType.translucent());

        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        vertexconsumer.addVertex(matrix4f, minX, minY, z).setColor(r, g, b, a);
        vertexconsumer.addVertex(matrix4f, minX, maxY, z).setColor(r, g, b, a);
        vertexconsumer.addVertex(matrix4f, maxX, maxY, z).setColor(r, g, b, a);
        vertexconsumer.addVertex(matrix4f, maxX, minY, z).setColor(r, g, b, a);


        MultiBufferSource.BufferSource buffers = client.renderBuffers().bufferSource();

        String text = "Hello, folk!";

        guiGraphics.pose().pushPose();
        float textScale = (5.0f / posClip.w) * fovScale;
        guiGraphics.pose().scale(textScale, textScale, 1.0f);

        client.font.drawInBatch(
                text,
                screenX / textScale,
                screenY / textScale,
                0xFFFFFF,
                true,
                guiGraphics.pose().last().pose(),
                buffers,
                Font.DisplayMode.NORMAL,
                0,
                15728880
        );

        guiGraphics.pose().popPose();

        buffers.endBatch();
    }

    private static Matrix4f getViewMatrix(Camera camera) {

        Vec3 camPos = camera.getPosition();
        Quaternionf camRot = camera.rotation();
        return new Matrix4f()
                .rotate(camRot.conjugate(new Quaternionf()))
                .translate((float) -camPos.x, (float) -camPos.y, (float) -camPos.z);

    }

}
