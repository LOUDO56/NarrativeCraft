package fr.loudo.narrativecraft.screens.story_manager.template;

import fr.loudo.narrativecraft.narrative.StoryDetails;
import fr.loudo.narrativecraft.utils.ImageFontConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;

public class StoryElementList extends ContainerObjectSelectionList<StoryElementList.Entry> {

    public StoryElementList(Minecraft minecraft, Screen screen, List<Button> buttons, List<StoryDetails> storyDetails) {
        super(minecraft, 240, screen.width, screen.height, 25);
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setWidth(170);
            StoryElementList.Entry entry;
            if (storyDetails.isEmpty()) {
                entry = new Entry(buttons.get(i), screen);
            } else {
                entry = new Entry(buttons.get(i), storyDetails.get(i), screen);
            }
            this.addEntry(entry);

        }
    }

    public static class Entry extends ContainerObjectSelectionList.Entry<Entry> {
        private final int gap = 5;
        private final int smallButtonWidth = 20;
        private final Button button;
        private final Screen screen;
        private Button editButton;
        private Button removeButton;

        public Entry(Button button, StoryDetails storyDetails, Screen screen) {
            this.button = button;
            this.screen = screen;
            this.editButton = Button.builder(ImageFontConstants.EDIT, button1 -> {
                EditInfoScreen editInfoScreen = new EditInfoScreen(screen, storyDetails);
                Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(editInfoScreen));
            }).width(smallButtonWidth).build();
            this.removeButton = Button.builder(ImageFontConstants.REMOVE, button1 -> {
                DeleteConfirmScreen deleteConfirmScreen = new DeleteConfirmScreen(screen, storyDetails);
                Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(deleteConfirmScreen));
            }).width(smallButtonWidth).build();
        }

        public Entry(Button button, Screen screen) {
            this.button = button;
            this.screen = screen;
        }

        public void render(GuiGraphics p_281311_, int p_94497_, int p_94498_, int p_94499_, int p_94500_, int p_94501_, int p_94502_, int p_94503_, boolean p_94504_, float p_94505_) {
            int totalWidth = button.getWidth();
            if(editButton != null && removeButton != null) {
                totalWidth += gap + editButton.getWidth() + gap + removeButton.getWidth();
            }
            int startX = this.screen.width / 2 - totalWidth / 2;
            int y = p_94498_;

            button.setPosition(startX, y);
            button.render(p_281311_, p_94502_, p_94503_, p_94505_);

            if(editButton != null && removeButton != null) {
                editButton.setPosition(button.getX() + button.getWidth() + gap, y);
                removeButton.setPosition(editButton.getX() + editButton.getWidth() + gap, y);
                editButton.render(p_281311_, p_94502_, p_94503_, p_94505_);
                removeButton.render(p_281311_, p_94502_, p_94503_, p_94505_);
            }

        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            if(editButton == null && removeButton == null) {
                return List.of(button);
            } else {
                return List.of(button, editButton, removeButton);
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            if(editButton == null && removeButton == null) {
                return List.of(button);
            } else {
                return List.of(button, editButton, removeButton);
            }
        }


    }
}
