package fr.loudo.narrativecraft.events;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

public class RenderWorldEvent {

    public static void renderWorld(WorldRenderContext worldRenderContext) {
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        OnRenderWorld.renderWorld(new PoseStack());
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }
}
