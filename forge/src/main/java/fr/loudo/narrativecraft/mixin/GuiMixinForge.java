package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.events.OnHudRender;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixinForge {
    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        OnHudRender.fadeRender(guiGraphics, deltaTracker);
        OnHudRender.saveIconRender(guiGraphics, deltaTracker);
    }
}
