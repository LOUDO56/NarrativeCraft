package fr.loudo.narrativecraft.screens.story_manager.template;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class StoryElementList extends ObjectSelectionList<StoryElementList.Entry> {

    public StoryElementList(Minecraft minecraft, Screen screen, List<Button> buttons) {
        super(minecraft, 240, screen.width, screen.height, 25);
        this.centerListVertically = false;
        for(Button button : buttons) {
            button.setWidth(210);
            StoryElementList.Entry entry = new StoryElementList.Entry(button, screen);
            this.addEntry(entry);
        }
    }

    public int getRowWidth() {
        return super.getRowWidth() + 50;
    }

    @Override
    protected boolean isSelectedItem(int index) {
        return false;
    }

    public class Entry extends ObjectSelectionList.Entry<StoryElementList.Entry> {
        private final AbstractWidget button;
        private final Screen screen;

        public Entry(AbstractWidget button, Screen screen) {
            this.button = button;
            this.screen = screen;
        }

        public void render(GuiGraphics p_281311_, int p_94497_, int p_94498_, int p_94499_, int p_94500_, int p_94501_, int p_94502_, int p_94503_, boolean p_94504_, float p_94505_) {
            button.setPosition(this.screen.width / 2 - button.getWidth() / 2, p_94498_);
            button.render(p_281311_, p_94502_, p_94503_, p_94505_);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int buttonId) {
            return this.button.mouseClicked(mouseX, mouseY, buttonId);
        }



        @Override
        public Component getNarration() {
            return Component.literal("");
        }

    }
}
