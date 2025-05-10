package fr.loudo.narrativecraft.events;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

public class RenderWorldEvent {
    public static void onRenderWorld(WorldRenderContext worldRenderContext) {
        OnRenderWorld.renderWorld(worldRenderContext.matrixStack());
    }
}
