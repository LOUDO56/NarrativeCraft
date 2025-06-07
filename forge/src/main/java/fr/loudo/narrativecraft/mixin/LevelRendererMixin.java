package fr.loudo.narrativecraft.mixin;

import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.events.OnHudRender;
import fr.loudo.narrativecraft.events.OnRenderWorld;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void renderLevel(GraphicsResourceAllocator p_367325_, DeltaTracker p_342180_, boolean p_109603_, Camera p_109604_, GameRenderer p_109605_, Matrix4f p_254120_, Matrix4f p_330527_, CallbackInfo ci) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Quaternionf quaternionf = camera.rotation().conjugate(new Quaternionf());
        Matrix4f frustumMatrix = new Matrix4f().rotation(quaternionf);
        Matrix4fStack matrix4fstack = RenderSystem.getModelViewStack();
        matrix4fstack.pushMatrix();
        matrix4fstack.mul(frustumMatrix);
        OnRenderWorld.renderWorld(new PoseStack());
        matrix4fstack.popMatrix();
    }
}
