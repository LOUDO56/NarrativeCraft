package fr.loudo.narrativecraft.screens.story_manager.chapters;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.ChapterManager;
import fr.loudo.narrativecraft.screens.story_manager.StoryElementScreen;
import fr.loudo.narrativecraft.screens.story_manager.scenes.ScenesScreen;
import fr.loudo.narrativecraft.screens.story_manager.template.StoryElementList;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ChaptersScreen extends StoryElementScreen {

    public ChaptersScreen() {
        super(null, Minecraft.getInstance().options, Translation.message("screen.chapter_manager.title"));
    }

    @Override
    protected void addContents() {
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
}
