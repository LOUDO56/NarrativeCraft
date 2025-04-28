package fr.loudo.narrativecraft.screens.cutscenes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;


import java.util.List;

public class CutsceneSettingsList extends ContainerObjectSelectionList<CutsceneSettingsList.Entry> {

    private static final int BUTTON_WIDTH = 170;
    private final OptionsSubScreen screen;

    public CutsceneSettingsList(Minecraft minecraft, int width, OptionsSubScreen screen) {
        super(minecraft, width, screen.layout.getContentHeight(), screen.layout.getHeaderHeight(), 25);
        this.centerListVertically = false;
        this.screen = screen;
    }

    public void addButton(AbstractWidget button) {
        button.setWidth(BUTTON_WIDTH);
        this.addEntry(new Entry(button, screen));
    }

    protected static class Entry extends ContainerObjectSelectionList.Entry<Entry> {
        private final AbstractWidget children;
        private final Screen screen;

        Entry(AbstractWidget children, Screen screen) {
            this.children = children;
            this.screen = screen;
        }

        // GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, int partialTick
        public void render(GuiGraphics p_281311_, int p_94497_, int p_94498_, int p_94499_, int p_94500_, int p_94501_, int p_94502_, int p_94503_, boolean p_94504_, float p_94505_) {

            children.setPosition(this.screen.width / 2 - (children.getWidth() / 2) ,p_94498_);
            children.render(p_281311_, p_94502_, p_94503_, p_94505_);

        }


        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(children);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(children);
        }
    }
}
