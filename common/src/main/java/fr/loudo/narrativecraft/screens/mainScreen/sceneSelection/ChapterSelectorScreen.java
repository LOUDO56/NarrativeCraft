package fr.loudo.narrativecraft.screens.mainScreen.sceneSelection;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.ChapterManager;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ChapterSelectorScreen extends StoryElementScreen {

    public ChapterSelectorScreen(Screen lastScreen) {
        super(lastScreen, Minecraft.getInstance().options, Translation.message("screen.chapter_manager.title"));
    }

    @Override
    protected void addTitle() {
        linearlayout = this.layout.addToHeader(LinearLayout.horizontal()).spacing(8);
        linearlayout.defaultCellSetting().alignVerticallyMiddle();
        linearlayout.addChild(new StringWidget(this.title, this.font));
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
                        SceneSelectorScreen screen = new SceneSelectorScreen(this, chapter);
                        minecraft.setScreen(screen);
                    }).build();

                    return new StoryElementList.StoryEntryData(button, chapter);
                })
                .toList();

        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, entries, false));
    }

    @Override
    protected void openFolder() {}

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
