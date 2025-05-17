package fr.loudo.narrativecraft.screens.storyManager.template;

import fr.loudo.narrativecraft.narrative.StoryDetails;
import fr.loudo.narrativecraft.utils.ImageFontConstants;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class StoryElementList extends ContainerObjectSelectionList<StoryElementList.Entry> {

    public StoryElementList(Minecraft minecraft, Screen screen, List<StoryEntryData> entriesData) {
        super(minecraft, 240, screen.width, screen.height, 25);
        for (StoryEntryData data : entriesData) {
            this.addEntry(new Entry(data, screen));
        }
    }

    public static class StoryEntryData {
        public final Button mainButton;
        public StoryDetails storyDetails;
        public List<Button> extraButtons;

        public StoryEntryData(Button mainButton, StoryDetails storyDetails, List<Button> extraButtons) {
            this.mainButton = mainButton;
            this.storyDetails = storyDetails;
            this.extraButtons = extraButtons;
        }

        public StoryEntryData(Button mainButton, StoryDetails storyDetails) {
            this.mainButton = mainButton;
            this.storyDetails = storyDetails;
            this.extraButtons = new ArrayList<>();
        }

        public StoryEntryData(Button mainButton) {
            this.mainButton = mainButton;
        }
    }

    public class Entry extends ContainerObjectSelectionList.Entry<Entry> {
        private final int gap = 5;
        private final Button mainButton;
        private final List<Button> buttons;
        private final Screen screen;

        public Entry(StoryEntryData data, Screen screen) {
            this.mainButton = data.mainButton;
            this.buttons = new ArrayList<>();
            this.screen = screen;
            buttons.add(mainButton);

            if (data.storyDetails != null) {
                buttons.add(createEditButton(data.storyDetails));
                buttons.add(createRemoveButton(data.storyDetails));
            }

            if (data.extraButtons != null) {
                buttons.addAll(data.extraButtons);
            }
        }

        private Button createEditButton(StoryDetails details) {
            return Button.builder(ImageFontConstants.EDIT, btn -> {
                Minecraft.getInstance().setScreen(new EditInfoScreen(screen, details));
            }).width(20).build();
        }

        private Button createRemoveButton(StoryDetails details) {
            return Button.builder(ImageFontConstants.REMOVE, btn -> {
                ConfirmScreen confirm = new ConfirmScreen(b -> {
                    if (b) {
                        details.remove();
                    }
                    Minecraft.getInstance().setScreen(details.reloadScreen());
                }, Component.literal(""), Translation.message("global.confirm_delete"),
                        CommonComponents.GUI_YES, CommonComponents.GUI_CANCEL);
                Minecraft.getInstance().setScreen(confirm);
            }).width(20).build();
        }

        @Override
        public void render(GuiGraphics p_281311_, int p_94497_, int p_94498_, int p_94499_, int p_94500_, int p_94501_, int p_94502_, int p_94503_, boolean p_94504_, float p_94505_) {
            int totalWidth = buttons.stream().mapToInt(Button::getWidth).sum() + (buttons.size() - 1) * gap;
            int x = screen.width / 2 - totalWidth / 2;
            int y = p_94498_;

            for (Button button : buttons) {
                button.setPosition(x, y);
                button.render(p_281311_, p_94502_, p_94503_, p_94505_);
                x += button.getWidth() + gap;
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return buttons;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return buttons;
        }
    }
}

