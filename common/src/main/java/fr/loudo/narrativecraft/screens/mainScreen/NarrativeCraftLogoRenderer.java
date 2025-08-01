package fr.loudo.narrativecraft.screens.mainScreen;

import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

public class NarrativeCraftLogoRenderer {

    public static final ResourceLocation LOGO = ResourceLocation.withDefaultNamespace("textures/narrativecraft_logo.png");

    private final ResourceLocation resourceLocation;
    private int[] logoRes;
    private int imageHeight;

    public NarrativeCraftLogoRenderer(ResourceLocation resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    public void init() {
        logoRes = Utils.getImageResolution(LOGO);
        if(logoRes != null) {
            imageHeight = Utils.getDynamicHeight(logoRes, 256);
        }
    }

    public boolean logoExists() {
        return Utils.resourceExists(resourceLocation);
    }

    public void render(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                LOGO,
                x, y,
                0, 0,
                256, imageHeight,
                256, imageHeight,
                ARGB.colorFromFloat(1, 1, 1, 1)
        );
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public int[] getLogoRes() {
        return logoRes;
    }
}
