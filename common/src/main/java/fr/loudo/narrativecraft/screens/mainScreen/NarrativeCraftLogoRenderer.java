package fr.loudo.narrativecraft.screens.mainScreen;

import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

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
                LOGO,
                x, y,
                0f, 0f,
                256, imageHeight,
                256, imageHeight
        );
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public int[] getLogoRes() {
        return logoRes;
    }
}
