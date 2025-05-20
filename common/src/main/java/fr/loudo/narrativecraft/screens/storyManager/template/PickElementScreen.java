package fr.loudo.narrativecraft.screens.storyManager.template;

import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class PickElementScreen extends Screen {

    private static final Component AVAILABLE_TITLE = Component.translatable("pack.available.title");
    private static final Component SELECTED_TITLE = Component.translatable("pack.selected.title");
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final Screen lastScreen;
    private final List<? extends NarrativeEntry> narrativeEntry1, narrativeEntry2;
    private Button moveButton, doneButton;
    private TransferableStorySelectionList availableList, selectedList;
    private StringWidget availableString, selectedString, headTitle;
    private Component availableMessage, selectedMessage, selector;
    Consumer<List<TransferableStorySelectionList.Entry>> onDone;

    public PickElementScreen(Screen lastScreen,
                             Component title,
                             Component selector,
                             List<? extends NarrativeEntry> narrativeEntry1,
                             List<? extends NarrativeEntry> narrativeEntry2,
                             Consumer<List<TransferableStorySelectionList.Entry>> onDone
    ) {
        super(title);
        this.selector = selector;
        this.narrativeEntry1 = narrativeEntry1;
        this.narrativeEntry2 = narrativeEntry2;
        this.onDone = onDone;
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        this.availableList = this.addRenderableWidget(new TransferableStorySelectionList(this.minecraft, narrativeEntry1, 200, 240));
        this.selectedList = this.addRenderableWidget(new TransferableStorySelectionList(this.minecraft, narrativeEntry2, 200, 240));
        this.availableList.setOtherList(selectedList);
        this.selectedList.setOtherList(availableList);
        this.moveButton = this.addRenderableWidget(Button.builder(Component.literal("◀"), button -> {
            if (availableList.getSelected() != null) {
                TransferableStorySelectionList.Entry selected = availableList.getSelected();
                availableList.children().remove(selected);
                selectedList.children().add(selected);
                selectedList.setSelected(selected);
                availableList.setSelected(null);
                moveButton.setMessage(Component.literal("◀"));
            } else if (selectedList.getSelected() != null) {
                TransferableStorySelectionList.Entry selected = selectedList.getSelected();
                selectedList.children().remove(selected);
                availableList.children().add(selected);
                availableList.setSelected(selected);
                selectedList.setSelected(null);
                moveButton.setMessage(Component.literal("▶"));
            }
            availableList.refreshScrollAmount();
            selectedList.refreshScrollAmount();
            moveButton.active = availableList.getSelected() != null || selectedList.getSelected() != null;

        }).width(20).build());

        moveButton.active = false;
        availableMessage = Component.literal(selector.getString() + " " + AVAILABLE_TITLE.getString());
        selectedMessage = Component.literal(selector.getString() + " " + SELECTED_TITLE.getString());
        availableString = this.addRenderableWidget(ScreenUtils.text(availableMessage, this.font, 0, 0));
        selectedString = this.addRenderableWidget(ScreenUtils.text(selectedMessage, this.font, 0, 0));
        doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
            onDone.accept(selectedList.children());
        }).width(200).build());
        headTitle = this.addRenderableWidget(ScreenUtils.text(title, this.font, 0, 0));
        repositionElements();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(lastScreen);
    }

    protected void repositionElements() {
        headTitle.setPosition(this.width / 2 - this.font.width(title) / 2, 12);
        availableList.setX(this.width / 2 - 15 - 200);
        availableList.setY(availableList.getY() + 15);
        selectedList.setX(this.width / 2 + 15);
        selectedList.setY(selectedList.getY() + 15);
        moveButton.setPosition(this.width / 2 - moveButton.getWidth() / 2, selectedList.getY() + selectedList.getHeight() / 2 - moveButton.getHeight() / 2);
        availableString.setPosition(availableList.getX() + availableList.getWidth() / 2 - this.font.width(availableMessage) / 2, availableList.getY() - 15);
        selectedString.setPosition(selectedList.getX() + selectedList.getWidth() / 2 - this.font.width(selectedMessage) / 2, selectedList.getY() - 15);
        doneButton.setPosition(this.width / 2 - 200 / 2, this.height - 25);
    }

    public class TransferableStorySelectionList extends ObjectSelectionList<TransferableStorySelectionList.Entry> {

        private TransferableStorySelectionList otherList;

        public TransferableStorySelectionList(Minecraft minecraft, List<? extends NarrativeEntry> narrativeEntries, int width, int height) {
            super(minecraft, width, height, 33, 18);
            for (NarrativeEntry narrativeEntry : narrativeEntries) {
                Entry entry = new Entry(narrativeEntry);
                this.addEntry(entry);
            }
        }

        public void setOtherList(TransferableStorySelectionList otherList) {
            this.otherList = otherList;
        }


        @Override
        protected int scrollBarX() {
            return this.getX() + this.width - 6;
        }

        @Override
        public void setSelected(@Nullable PickElementScreen.TransferableStorySelectionList.Entry selected) {
            super.setSelected(selected);
            if (otherList != null && selected != null) {
                otherList.setSelected(null);
                if(Objects.equals(otherList, PickElementScreen.this.selectedList)) {;
                    PickElementScreen.this.moveButton.setMessage(Component.literal("▶"));
                } else {
                    PickElementScreen.this.moveButton.setMessage(Component.literal("◀"));
                }
                PickElementScreen.this.moveButton.active = true;
            }
        }

        @Override
        public boolean mouseDragged(double p_313749_, double p_313887_, int p_313839_, double p_313844_, double p_313686_) {
            return super.mouseDragged(p_313749_, p_313887_, p_313839_, p_313844_, p_313686_);
        }

        public class Entry extends ObjectSelectionList.Entry<Entry> {

            private final NarrativeEntry narrativeEntry;

            public Entry(NarrativeEntry narrativeEntry) {
                this.narrativeEntry = narrativeEntry;
            }

            @Override
            public Component getNarration() {
                return Component.literal(narrativeEntry.getName());
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
                guiGraphics.drawCenteredString(minecraft.font, narrativeEntry.getName(), left + 4 + TransferableStorySelectionList.this.width / 2, top + height / 2 - 4, -1);
            }

            public NarrativeEntry getNarrativeEntry() {
                return narrativeEntry;
            }
        }
    }
}
