package fr.loudo.narrativecraft.screens.story_manager.scenes;

import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.StoryDetails;
import fr.loudo.narrativecraft.screens.story_manager.chapters.ChaptersScreen;
import fr.loudo.narrativecraft.screens.story_manager.template.EditInfoScreen;
import fr.loudo.narrativecraft.screens.story_manager.template.StoryElementList;
import fr.loudo.narrativecraft.utils.ImageFontConstants;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ScenesScreen extends OptionsSubScreen {

    private final Chapter chapter;
    private StoryElementList storyElementList;

    public ScenesScreen(Chapter chapter) {
        super(null, Minecraft.getInstance().options, Translation.message("screen.scene_manager.title", chapter.getIndex()));
        this.chapter = chapter;
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
    public void onClose() {
        ChaptersScreen screen = new ChaptersScreen();
        this.minecraft.setScreen(screen);
    }

    @Override
    protected void addContents() {
        List<Button> buttons = new ArrayList<>();
        List<StoryDetails> storyDetails = new ArrayList<>();
        for(Scene scene : chapter.getSceneList()) {
            Button button = Button.builder(Component.literal(String.valueOf(scene.getName())), button1 -> {
                ScenesMenuScreen screen = new ScenesMenuScreen(scene);
                this.minecraft.setScreen(screen);
            }).build();
            buttons.add(button);
            storyDetails.add(scene);
        }
        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, buttons, storyDetails));
    }

    @Override
    protected void addFooter() {
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_BACK, (p_345997_) -> this.onClose()).width(200).build());
    }

    @Override
    protected void addOptions() {}

    protected void repositionElements() {
        super.repositionElements();
        this.storyElementList.updateSize(this.width, this.layout);
    }

    public Chapter getChapter() {
        return chapter;
    }
}
