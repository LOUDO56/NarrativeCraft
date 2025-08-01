package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.ResourceLocation;

public class HudRender {

    private static final ResourceLocation DIALOG_HUD = ResourceLocation.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "dialog-hud");
    private static final ResourceLocation FADE_HUD = ResourceLocation.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "fade-hud");
    private static final ResourceLocation SAVE_ICON_HUD = ResourceLocation.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "save-icon-hud");
    private static final ResourceLocation KEYFRAME_CONTROLLER_BASE_INFO = ResourceLocation.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "keyframe-controller-base-hud");
    private static final ResourceLocation BORDER_HUD = ResourceLocation.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "border-hud");
    private static final ResourceLocation LOADING_HUD = ResourceLocation.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "loading-hud");

    // Does not work for some reason?
    public static void register() {
        HudElementRegistry.addLast(FADE_HUD, OnHudRender::fadeRender);
        HudElementRegistry.addLast(SAVE_ICON_HUD, OnHudRender::saveIconRender);
        HudElementRegistry.addLast(DIALOG_HUD, OnHudRender::dialogHud);
        HudElementRegistry.addLast(KEYFRAME_CONTROLLER_BASE_INFO, OnHudRender::keyframeControllerBaseRender);
        HudElementRegistry.addLast(BORDER_HUD, OnHudRender::borderHud);
        HudElementRegistry.addLast(LOADING_HUD, OnHudRender::loadingHud);
    }

}
