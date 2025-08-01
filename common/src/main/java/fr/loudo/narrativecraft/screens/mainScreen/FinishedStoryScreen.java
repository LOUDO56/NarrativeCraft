package fr.loudo.narrativecraft.screens.mainScreen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class FinishedStoryScreen extends Screen {

    private static final ResourceLocation WINDOW_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/advancements/window.png");
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    public FinishedStoryScreen() {
        super(Component.literal("Finished Story Screen"));
    }

    @Override
    public void onClose() {
        MainScreen mainScreen = new MainScreen(false, false);
        minecraft.setScreen(mainScreen);
    }

    @Override
    protected void init() {
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, (p_331557_) -> this.onClose()).width(200).build());
        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int x, int y, float partialTick) {
        super.render(guiGraphics, x, y, partialTick);
        int i = (this.width - 252) / 2;
        int j = (this.height - 140) / 2;
        this.renderInside(guiGraphics, x, y, i, j);
        this.renderWindow(guiGraphics, i, j);
    }

    private void renderInside(GuiGraphics guiGraphics, int mouseX, int mouseY, int offsetX, int offsetY) {
        int i = offsetX + 9 + 117;
        guiGraphics.fill(offsetX + 9, offsetY + 18, offsetX + 9 + 234, offsetY + 18 + 113, -16777216);
        int textPosY = offsetY + 18 + 56 - 4;
        guiGraphics.drawCenteredString(this.font, "Thanks for playing!", i, textPosY - minecraft.font.lineHeight / 2 - 2, -1);
        guiGraphics.drawCenteredString(this.font, "You can now select specific scenes.", i, textPosY + minecraft.font.lineHeight / 2 + 2, -1);


    }

    public void renderWindow(GuiGraphics guiGraphics, int offsetX, int offsetY) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, WINDOW_LOCATION, offsetX, offsetY, 0.0F, 0.0F, 252, 140, 256, 256);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
