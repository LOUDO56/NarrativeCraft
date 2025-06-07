package fr.loudo.narrativecraft.events;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;

public class RenderWorldEvent {
    public static void onRenderWorld(WorldRenderContext worldRenderContext) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Quaternionf quaternionf = camera.rotation().conjugate(new Quaternionf());
        Matrix4f frustumMatrix = new Matrix4f().rotation(quaternionf);
        Matrix4fStack matrix4fstack = RenderSystem.getModelViewStack();
        matrix4fstack.pushMatrix();
        matrix4fstack.mul(frustumMatrix);
        OnRenderWorld.renderWorld(worldRenderContext.matrixStack());
        matrix4fstack.popMatrix();
    }

}
