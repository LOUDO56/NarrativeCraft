package fr.loudo.narrativecraft.screens.storyManager.chapters;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.ChapterManager;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.screens.storyManager.scenes.ScenesScreen;
import fr.loudo.narrativecraft.screens.storyManager.template.StoryElementList;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ChaptersScreen extends StoryElementScreen {

    public ChaptersScreen() {
        super(null, Minecraft.getInstance().options, Translation.message("screen.chapter_manager.title"));
    }

    @Override
    protected void addFooter() {
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, (p_345997_) -> this.onClose()).width(200).build());
    }

    @Override
    protected void addContents() {
        ChapterManager chapterManager = NarrativeCraftMod.getInstance().getChapterManager();

        List<StoryElementList.StoryEntryData> entries = chapterManager.getChapters().stream()
                .map(chapter -> {
                    String label = String.valueOf(chapter.getIndex());
                    if (!chapter.getName().isEmpty()) {
                        label += " - " + chapter.getName();
                    }

                    Button button = Button.builder(Component.literal(label), b -> {
                        this.minecraft.setScreen(new ScenesScreen(chapter));
                    }).build();

                    return new StoryElementList.StoryEntryData(button, chapter);
                })
                .toList();

        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, entries));
    }

}
