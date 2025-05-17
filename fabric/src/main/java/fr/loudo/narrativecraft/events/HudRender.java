package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper;
import net.minecraft.resources.ResourceLocation;

public class HudRender {

    private static final ResourceLocation DIALOG_HUD = ResourceLocation.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "dialog-hud");

    public static void onHudRender(LayeredDrawerWrapper layeredDrawerWrapper) {
        layeredDrawerWrapper.attachLayerBefore(IdentifiedLayer.CROSSHAIR, DIALOG_HUD, OnHudRender::hudRender);
    }
}
