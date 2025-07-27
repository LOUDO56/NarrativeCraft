package fr.loudo.narrativecraft.screens.components;

import fr.loudo.narrativecraft.screens.mainScreen.MainScreen;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class CrashScreen extends Screen {

    private boolean crashFromStory;
    private CrashReport crashReport;
    private static final ResourceLocation WINDOW_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/advancements/window.png");
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    public CrashScreen(boolean crashFromStory, CrashReport crashReport) {
        super(Component.literal("Crash Screen"));
        this.crashFromStory = crashFromStory;
        this.crashReport = crashReport;
    }

    @Override
    public void onClose() {
        MainScreen mainScreen = new MainScreen(false, false);
        minecraft.setScreen(mainScreen);
    }

    @Override
    protected void init() {
        LinearLayout linearLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(4));
        linearLayout.addChild(Button.builder(Translation.message("screen.crash.button.open_crash"), (p_331557_) -> Util.getPlatform().openPath(crashReport.getSaveFile())).width(130).build());
        linearLayout.addChild(Button.builder(CommonComponents.GUI_DONE, (p_331557_) -> this.onClose()).width(130).build());
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
        String[] lines;
        if(crashFromStory) {
            lines = Translation.message("screen.crash.from_story").getString().split("\n");
        } else {
            lines = Translation.message("screen.crash.from_mod").getString().split("\n");
        }
        int textPosY = offsetY + 18 + 56 - 4 - (minecraft.font.lineHeight * lines.length + 2) / 2;
        guiGraphics.drawCenteredString(this.font, Translation.message("screen.crash.title").getString(), i, textPosY, -1);
        for (String line : lines) {
            textPosY += minecraft.font.lineHeight;
            guiGraphics.drawCenteredString(this.font, line, i, textPosY, -1);
        }
    }

    public void renderWindow(GuiGraphics guiGraphics, int offsetX, int offsetY) {
        guiGraphics.blit(WINDOW_LOCATION, offsetX, offsetY, 0.0F, 0.0F, 252, 140, 256, 256);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
