package fr.loudo.narrativecraft.screens.storyManager.components;

import fr.loudo.narrativecraft.narrative.NarrativeEntry;
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
        public NarrativeEntry narrativeEntry;
        public List<Button> extraButtons;

        public StoryEntryData(Button mainButton, NarrativeEntry narrativeEntry, List<Button> extraButtons) {
            this.mainButton = mainButton;
            this.narrativeEntry = narrativeEntry;
            this.extraButtons = extraButtons;
        }

        public StoryEntryData(Button mainButton, NarrativeEntry narrativeEntry) {
            this.mainButton = mainButton;
            this.narrativeEntry = narrativeEntry;
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
            this.screen = screen;
            this.mainButton = data.mainButton;
            this.buttons = new ArrayList<>();
            buttons.add(mainButton);

            if (data.narrativeEntry != null) {
                buttons.add(createEditButton(data.narrativeEntry));
                buttons.add(createRemoveButton(data.narrativeEntry));
            }

            if (data.extraButtons != null) {
                buttons.addAll(data.extraButtons);
            }
        }

        private Button createEditButton(NarrativeEntry details) {
            return Button.builder(ImageFontConstants.EDIT, btn -> {
                Minecraft.getInstance().setScreen(new EditInfoScreen(screen, details));
            }).width(20).build();
        }

        private Button createRemoveButton(NarrativeEntry details) {
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
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partial) {
            int totalWidth = buttons.stream().mapToInt(Button::getWidth).sum() + (buttons.size() - 1) * gap;
            int x = (screen.width / 2 - totalWidth / 2) - gap; //  this line is wrong but sorry my head hurts okay, those mouseX coord shit is driving me crazy

            for (Button button : buttons) {
                button.setPosition(x, top);
                button.render(graphics, mouseX, mouseY, partial);
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

