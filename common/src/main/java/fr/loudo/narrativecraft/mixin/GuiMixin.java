package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "renderChat", at = @At(value = "HEAD"), cancellable = true)
    private void renderChat(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
        boolean isDebug = false;
        if(storyHandler != null) {
            isDebug = storyHandler.isDebugMode();
        }
        if(NarrativeCraftMod.getInstance().isCutsceneMode() && !isDebug) ci.cancel();
    }

}
