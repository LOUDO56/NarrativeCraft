package fr.loudo.narrativecraft.screens.mainScreen.sceneSelection;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.MainScreenController;
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
        List<StoryElementList.StoryEntryData> entries = chapter.getSortedSceneList().stream()
                .map(scene -> {
                    Button button = Button.builder(Component.literal(scene.getName()), b -> {
                        PlayerSession playerSession = NarrativeCraftMod.getInstance().getPlayerSession();
                        if(playerSession.getKeyframeControllerBase() instanceof MainScreenController mainScreenController) {
                            mainScreenController.stopSession(false);
                        }
                        minecraft.getSoundManager().stop(MainScreen.MUSIC_INSTANCE);
                        minecraft.setScreen(null);
                        new StoryHandler(chapter, scene).start();
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
