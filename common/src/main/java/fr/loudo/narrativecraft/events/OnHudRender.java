package fr.loudo.narrativecraft.events;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

public class OnHudRender {

    public static Vec3 entityPos;

    public static void hudRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if(entityPos == null) return;
        Minecraft client = Minecraft.getInstance();
        Matrix4f projection = client.gameRenderer.getProjectionMatrix(client.options.fov().get());
        Matrix4f view = getViewMatrix(client.gameRenderer.getMainCamera());
        Vector4f posWorld = new Vector4f((float) entityPos.x, (float) entityPos.y, (float) entityPos.z, 1.0f);
        Vector4f posClip = new Vector4f(posWorld);
        view.transform(posClip);
        projection.transform(posClip);

        float ndcX = posClip.x / posClip.w;
        float ndcY = posClip.y / posClip.w;
        float ndcZ = posClip.z / posClip.w;
        float ndcW = 1.0f;

        int screenWidth = Minecraft.getInstance().getWindow().getScreenWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getScreenHeight();

        float screenX = (ndcX + 1.0f) / 2.0f * screenWidth;
        float screenY = (1.0f - ndcY) / 2.0f * screenHeight;

        guiGraphics.drawString(client.font, "Hello", (int) screenX, (int) screenY, 0xFFFFFF);

    }

    private static Matrix4f getViewMatrix(Camera camera) {

        Vec3 camPos = camera.getPosition();
        Quaternionf camRot = camera.rotation();
        return new Matrix4f()
                .rotate(camRot.conjugate(new Quaternionf()))
                .translate((float) -camPos.x, (float) -camPos.y, (float) -camPos.z);

    }

}
