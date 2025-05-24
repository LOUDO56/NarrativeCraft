package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper;
import net.minecraft.resources.ResourceLocation;

public class HudRender {

    private static final ResourceLocation DIALOG_HUD = ResourceLocation.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "dialog-hud");
    private static final ResourceLocation FADE_HUD = ResourceLocation.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "fade-hud");

    public static void onHudRender(LayeredDrawerWrapper layeredDrawerWrapper) {
        layeredDrawerWrapper.attachLayerBefore(IdentifiedLayer.CROSSHAIR, DIALOG_HUD, OnHudRender::hudRender);
    }

    public static void fadeHUDRender(LayeredDrawerWrapper layeredDrawerWrapper) {
        layeredDrawerWrapper.attachLayerAfter(IdentifiedLayer.CROSSHAIR, FADE_HUD, OnHudRender::fadeRender);
    }
}
