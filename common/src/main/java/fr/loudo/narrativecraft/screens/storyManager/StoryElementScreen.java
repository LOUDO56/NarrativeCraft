package fr.loudo.narrativecraft.screens.storyManager;

import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.screens.components.DialogCustomScreen;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.utils.ImageFontConstants;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public abstract class StoryElementScreen extends OptionsSubScreen {

    public static final int SCENE_NAME_COLOR = 0x5896ED; // BLUE
    public static final int ANIMATION_NAME_COLOR = 0xE0DE65; // YELLOW
    public static final int CUTSCENE_NAME_COLOR = 0xE34045; // RED
    public static final int SUBSCENE_NAME_COLOR = 0x94E866; // GREEN

    protected StoryElementList storyElementList;
    protected List<Button> buttons;
    protected List<NarrativeEntry> narrativeEntries;

    protected LinearLayout linearlayout;

    public StoryElementScreen(Screen lastScreen, Options options, Component title) {
        super(lastScreen, options, title);
        buttons = new ArrayList<>();
        narrativeEntries = new ArrayList<>();
    }

    @Override
    protected void addTitle() {
        linearlayout = this.layout.addToHeader(LinearLayout.horizontal()).spacing(8);
        linearlayout.defaultCellSetting().alignVerticallyMiddle();
        linearlayout.addChild(new StringWidget(this.title, this.font));
        linearlayout.addChild(Button.builder(ImageFontConstants.ADD, button -> {
            EditInfoScreen screen = new EditInfoScreen(this);
            this.minecraft.setScreen(screen);
        }).width(25).build());
        linearlayout.addChild(Button.builder(ImageFontConstants.FOLDER, button -> {
            openFolder();
        }).width(25).build());
    }

    @Override
    protected void repositionElements() {
        super.repositionElements();
        this.storyElementList.updateSize(this.width, this.layout);
    }

    @Override
    protected void addFooter() {
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_BACK, (p_345997_) -> this.onClose()).width(200).build());
    }

    @Override
    protected void addOptions() {}

    protected abstract void openFolder();
}
