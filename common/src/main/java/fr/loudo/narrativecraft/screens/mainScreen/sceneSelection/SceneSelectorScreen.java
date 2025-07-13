package fr.loudo.narrativecraft.screens.mainScreen.sceneSelection;

import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.screens.mainScreen.MainScreen;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class SceneSelectorScreen extends StoryElementScreen {

    private final Chapter chapter;

    public SceneSelectorScreen(Screen lastScreen, Chapter chapter) {
        super(lastScreen, Minecraft.getInstance().options, Translation.message("screen.scene_manager.title", chapter.getIndex()));
        this.chapter = chapter;
    }

    @Override
    protected void addTitle() {
        linearlayout = this.layout.addToHeader(LinearLayout.horizontal()).spacing(8);
        linearlayout.defaultCellSetting().alignVerticallyMiddle();
        linearlayout.addChild(new StringWidget(this.title, this.font));
    }

    @Override
    protected void addContents() {
        List<StoryElementList.StoryEntryData> entries = chapter.getSceneList().stream()
                .map(scene -> {
                    Button button = Button.builder(Component.literal(scene.getName()), b -> {
                        StoryHandler storyHandler = new StoryHandler(chapter, scene);
                        storyHandler.start();
                        minecraft.getSoundManager().stop(MainScreen.MUSIC_INSTANCE);
                        minecraft.setScreen(null);
                    }).build();
                    return new StoryElementList.StoryEntryData(button, scene);
                }).toList();
        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, entries, false));
    }

    @Override
    protected void openFolder() {}

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
