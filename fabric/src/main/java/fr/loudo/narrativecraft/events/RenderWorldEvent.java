package fr.loudo.narrativecraft.events;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

public class RenderWorldEvent {
    public static void renderWorld(WorldRenderContext worldRenderContext) {
        OnRenderWorld.renderWorld(new PoseStack());
    }
}
