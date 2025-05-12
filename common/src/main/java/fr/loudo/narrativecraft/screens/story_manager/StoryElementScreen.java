package fr.loudo.narrativecraft.screens.story_manager;

import fr.loudo.narrativecraft.narrative.StoryDetails;
import fr.loudo.narrativecraft.screens.story_manager.template.EditInfoScreen;
import fr.loudo.narrativecraft.screens.story_manager.template.StoryElementList;
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

public class StoryElementScreen extends OptionsSubScreen {

    protected StoryElementList storyElementList;
    protected List<Button> buttons;
    protected List<StoryDetails> storyDetails;

    public StoryElementScreen(Screen lastScreen, Options options, Component title) {
        super(lastScreen, options, title);
        buttons = new ArrayList<>();
        storyDetails = new ArrayList<>();
    }

    @Override
    protected void addTitle() {
        LinearLayout linearlayout = this.layout.addToHeader(LinearLayout.horizontal()).spacing(8);
        linearlayout.defaultCellSetting().alignVerticallyMiddle();
        linearlayout.addChild(new StringWidget(this.title, this.font));
        linearlayout.addChild(Button.builder(ImageFontConstants.ADD, button -> {
            EditInfoScreen screen = new EditInfoScreen(this);
            this.minecraft.setScreen(screen);
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
}
