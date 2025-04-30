package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.narrative.dialog.Dialog;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

public class OnHudRender {

    public static Dialog dialog;

    public static void hudRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if(dialog == null) return;
        dialog.render(guiGraphics, deltaTracker);
    }

}
