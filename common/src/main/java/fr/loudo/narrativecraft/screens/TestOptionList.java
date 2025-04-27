package fr.loudo.narrativecraft.screens;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;


import java.util.List;

public class TestOptionList extends ContainerObjectSelectionList<TestOptionList.Entry> {

    private static final int BIG_BUTTON_WIDTH = 310;
    private static final int DEFAULT_ITEM_HEIGHT = 25;
    private final OptionsSubScreen screen;

    public TestOptionList(Minecraft minecraft, int width, OptionsSubScreen screen) {
        super(minecraft, width, screen.layout.getContentHeight(), screen.layout.getHeaderHeight(), 25);
        this.centerListVertically = false;
        this.screen = screen;
    }

    public void addButton(Button button) {
        button.setWidth(230);
        this.addEntry(new Entry(List.of(button), screen));
    }

    protected static class Entry extends ContainerObjectSelectionList.Entry<Entry> {
        private final List<AbstractWidget> children;
        private final Screen screen;
        private static final int X_OFFSET = 160;

        Entry(List<AbstractWidget> children, Screen screen) {
            this.children = ImmutableList.copyOf(children);
            this.screen = screen;
        }

        public void render(GuiGraphics p_281311_, int p_94497_, int p_94498_, int p_94499_, int p_94500_, int p_94501_, int p_94502_, int p_94503_, boolean p_94504_, float p_94505_) {
            int i = 0;
            int j = this.screen.width / 2 - 155;

            for(AbstractWidget abstractwidget : this.children) {
                abstractwidget.setPosition(j + i, p_94498_);
                abstractwidget.render(p_281311_, p_94502_, p_94503_, p_94505_);
                i += 160;
            }

        }

        public List<? extends GuiEventListener> children() {
            return this.children;
        }

        public List<? extends NarratableEntry> narratables() {
            return this.children;
        }
    }
}
