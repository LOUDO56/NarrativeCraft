package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "renderCrosshair", at = @At(value = "HEAD"), cancellable = true)
    private void renderCrosshair(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if(NarrativeCraftMod.getInstance().isCutsceneMode()) ci.cancel();
    }

    @Inject(method = "renderItemHotbar", at = @At(value = "HEAD"), cancellable = true)
    private void renderItemHotbar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if(NarrativeCraftMod.getInstance().isCutsceneMode()) ci.cancel();
    }

    @Inject(method = "renderChat", at = @At(value = "HEAD"), cancellable = true)
    private void renderChat(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if(NarrativeCraftMod.getInstance().isCutsceneMode()) ci.cancel();
    }

}
