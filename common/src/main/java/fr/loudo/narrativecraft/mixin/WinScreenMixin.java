package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.screens.credits.CreditsScreen;
import fr.loudo.narrativecraft.screens.mainScreen.NarrativeCraftLogoRenderer;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(WinScreen.class)
public class WinScreenMixin {

    @Mutable
    @Shadow @Final private float unmodifiedScrollSpeed;

    @Shadow private float scrollSpeed;

    @Inject(method = "init", at = @At(value = "TAIL"))
    private void narrativecraft$creditSpeed(CallbackInfo ci) {
        WinScreen winScreen = (WinScreen) (Object) this;
        if(winScreen instanceof CreditsScreen) {
            this.unmodifiedScrollSpeed = 1.4F;
            this.scrollSpeed = this.unmodifiedScrollSpeed;
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/LogoRenderer;renderLogo(Lnet/minecraft/client/gui/GuiGraphics;IFI)V"))
    private void narrativecraft$renderLogo(LogoRenderer instance, GuiGraphics guiGraphics, int p_281512_, float p_281290_, int p_282296_) {
        WinScreen winScreen = (WinScreen) (Object) this;
        if (winScreen instanceof CreditsScreen creditsScreen) {
            if (Utils.resourceExists(CreditsScreen.LOGO)) {
                NarrativeCraftLogoRenderer narrativeCraftLogoRenderer = NarrativeCraftMod.getInstance().getNarrativeCraftLogoRenderer();
                guiGraphics.blit(
                        RenderType::guiTextured,
                        CreditsScreen.LOGO,
                        creditsScreen.width / 2 - 128, creditsScreen.height + 50 - narrativeCraftLogoRenderer.getImageHeight() / 2,
                        0, 0,
                        256, narrativeCraftLogoRenderer.getImageHeight(),
                        256, narrativeCraftLogoRenderer.getImageHeight(),
                        ARGB.colorFromFloat(1, 1, 1, 1)
                );
            }
        } else {
            instance.renderLogo(guiGraphics, winScreen.width, 1.0F, winScreen.height + 50);
        }
    }
}
