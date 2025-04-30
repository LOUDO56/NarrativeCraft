package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.narrative.dialog.Dialog;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public class OnHudRender {

    public static Dialog dialog;

    public static void hudRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if(dialog == null) return;
        LocalPlayer entity = Minecraft.getInstance().player;
        dialog.updateTextPosition(deltaTracker);
        dialog.render(guiGraphics, deltaTracker);
    }

}
