package fr.loudo.narrativecraft.screens.story_manager;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.ChapterManager;
import fr.loudo.narrativecraft.screens.story_manager.template.StoryElementList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ChaptersScreen extends OptionsSubScreen {

    private StoryElementList storyElementList;

    public ChaptersScreen() {
        super(null, Minecraft.getInstance().options, Component.literal("Chapters List"));
    }

    @Override
    protected void addContents() {
        List<Button> buttons = new ArrayList<>();
        ChapterManager chapterManager = NarrativeCraftMod.getInstance().getChapterManager();
        for(Chapter chapter : chapterManager.getChapters()) {
            Button button = Button.builder(Component.literal(String.valueOf(chapter.getIndex())), button1 -> {
                ScenesScreen screen = new ScenesScreen(this, chapter);
                this.minecraft.setScreen(screen);
            }).build();
            buttons.add(button);
        }
        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, buttons));
    }

    @Override
    protected void addOptions() {}

    protected void repositionElements() {
        super.repositionElements();
        this.storyElementList.updateSize(this.width, this.layout);
    }
}
