package fr.loudo.narrativecraft.screens.storyManager.scenes;

import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.screens.storyManager.chapters.ChaptersScreen;
import fr.loudo.narrativecraft.screens.storyManager.template.StoryElementList;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ScenesScreen extends StoryElementScreen {

    private final Chapter chapter;

    public ScenesScreen(Chapter chapter) {
        super(null, Minecraft.getInstance().options, Translation.message("screen.scene_manager.title", chapter.getIndex()));
        this.chapter = chapter;
    }

    @Override
    public void onClose() {
        ChaptersScreen screen = new ChaptersScreen();
        this.minecraft.setScreen(screen);
    }

    @Override
    protected void addContents() {
        List<StoryElementList.StoryEntryData> entries = chapter.getSceneList().stream()
                .map(scene -> {
                    Button button = Button.builder(Component.literal(scene.getName()), b -> {
                        this.minecraft.setScreen(new ScenesMenuScreen(scene));
                    }).build();
                    return new StoryElementList.StoryEntryData(button, scene);
                }).toList();
        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, entries));
    }


    protected void repositionElements() {
        super.repositionElements();
        this.storyElementList.updateSize(this.width, this.layout);
    }

    public Chapter getChapter() {
        return chapter;
    }

}
