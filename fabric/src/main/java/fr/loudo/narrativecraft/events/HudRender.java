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
    private static final ResourceLocation BORDER_HUD = ResourceLocation.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "border-hud");

    public static void fadeHUDRender(LayeredDrawerWrapper layeredDrawerWrapper) {
        layeredDrawerWrapper.addLayer(IdentifiedLayer.of(FADE_HUD, OnHudRender::fadeRender));
    }

    public static void saveIconRender(LayeredDrawerWrapper layeredDrawerWrapper) {
        layeredDrawerWrapper.addLayer(IdentifiedLayer.of(SAVE_ICON_HUD, OnHudRender::saveIconRender));
    }

    public static void dialogHud(LayeredDrawerWrapper layeredDrawerWrapper) {
        layeredDrawerWrapper.addLayer(IdentifiedLayer.of(DIALOG_HUD, OnHudRender::dialogHud));
    }

    public static void keyframeControllerBaseHUDRender(LayeredDrawerWrapper layeredDrawerWrapper) {
        layeredDrawerWrapper.addLayer(IdentifiedLayer.of(KEYFRAME_CONTROLLER_BASE_INFO, OnHudRender::keyframeControllerBaseRender));
    }

    public static void borderHud(LayeredDrawerWrapper layeredDrawerWrapper) {
        layeredDrawerWrapper.addLayer(IdentifiedLayer.of(BORDER_HUD, OnHudRender::borderHud));
    }
}
