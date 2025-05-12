package fr.loudo.narrativecraft.screens.story_manager.chapters;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.ChapterManager;
import fr.loudo.narrativecraft.screens.story_manager.scenes.ScenesScreen;
import fr.loudo.narrativecraft.screens.story_manager.template.EditInfoScreen;
import fr.loudo.narrativecraft.narrative.StoryDetails;
import fr.loudo.narrativecraft.screens.story_manager.template.StoryElementList;
import fr.loudo.narrativecraft.utils.ImageFontConstants;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ChaptersScreen extends OptionsSubScreen {

    private StoryElementList storyElementList;

    public ChaptersScreen() {
        super(null, Minecraft.getInstance().options, Translation.message("screen.chapter_manager.title"));
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
    protected void addContents() {
        List<Button> buttons = new ArrayList<>();
        List<StoryDetails> storyDetails = new ArrayList<>();
        ChapterManager chapterManager = NarrativeCraftMod.getInstance().getChapterManager();
        for(Chapter chapter : chapterManager.getChapters()) {
            String message = String.valueOf(chapter.getIndex());
            if(!chapter.getName().isEmpty()) {
                message +=  " - " + chapter.getName();
            }
            Button button = Button.builder(Component.literal(message), button1 -> {
                ScenesScreen screen = new ScenesScreen(chapter);
                this.minecraft.setScreen(screen);
            }).build();
            buttons.add(button);
            storyDetails.add(chapter);
        }
        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, buttons, storyDetails));
    }

    @Override
    protected void addOptions() {}

    protected void repositionElements() {
        super.repositionElements();
        this.storyElementList.updateSize(this.width, this.layout);
    }
}
