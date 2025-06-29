package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper;
import net.minecraft.resources.ResourceLocation;

public class HudRender {

    private static final ResourceLocation DIALOG_HUD = ResourceLocation.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "dialog-hud");
    private static final ResourceLocation FADE_HUD = ResourceLocation.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "fade-hud");
    private static final ResourceLocation SAVE_ICON_HUD = ResourceLocation.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "save-icon-hud");
    private static final ResourceLocation KEYFRAME_CONTROLLER_BASE_INFO = ResourceLocation.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "keyframe-controller-base-hud");

    public static void onHudRender(LayeredDrawerWrapper layeredDrawerWrapper) {
        layeredDrawerWrapper.attachLayerBefore(IdentifiedLayer.CROSSHAIR, DIALOG_HUD, OnHudRender::hudRender);
    }

    public static void fadeHUDRender(LayeredDrawerWrapper layeredDrawerWrapper) {
        layeredDrawerWrapper.attachLayerAfter(IdentifiedLayer.CROSSHAIR, SAVE_ICON_HUD, OnHudRender::saveIconRender);
        layeredDrawerWrapper.attachLayerAfter(IdentifiedLayer.CROSSHAIR, FADE_HUD, OnHudRender::fadeRender);
    }

    public static void keyframeControllerBaseHUDRender(LayeredDrawerWrapper layeredDrawerWrapper) {
        layeredDrawerWrapper.attachLayerBefore(IdentifiedLayer.HOTBAR_AND_BARS, KEYFRAME_CONTROLLER_BASE_INFO, OnHudRender::keyframeControllerBaseRender);
    }
}
