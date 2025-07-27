package fr.loudo.narrativecraft.events;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

public class HudRenderEvent {

    public static void hudRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        OnHudRender.dialogHud(guiGraphics, deltaTracker);
        OnHudRender.fadeRender(guiGraphics, deltaTracker);
        OnHudRender.saveIconRender(guiGraphics, deltaTracker);
        OnHudRender.keyframeControllerBaseRender(guiGraphics, deltaTracker);
        OnHudRender.borderHud(guiGraphics, deltaTracker);
        OnHudRender.loadingHud(guiGraphics, deltaTracker);
    }
}
