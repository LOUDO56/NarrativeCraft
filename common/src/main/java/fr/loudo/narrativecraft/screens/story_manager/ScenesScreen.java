package fr.loudo.narrativecraft.screens.story_manager;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.ChapterManager;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.screens.story_manager.template.StoryElementList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.FontOptionsScreen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ScenesScreen extends OptionsSubScreen {

    private final Chapter chapter;
    private StoryElementList storyElementList;

    public ScenesScreen(Screen lastScreen, Chapter chapter) {
        super(lastScreen, Minecraft.getInstance().options, Component.literal("Scenes list of chapter " + chapter.getIndex()));
        this.chapter = chapter;
    }

    @Override
    protected void addTitle() {
        LinearLayout linearlayout = ((LinearLayout)this.layout.addToHeader(LinearLayout.horizontal())).spacing(8);
        linearlayout.defaultCellSetting().alignVerticallyMiddle();
        linearlayout.addChild(new StringWidget(this.title, this.font));
        linearlayout.addChild(Button.builder(Component.literal("Edit Chapter"), button -> {}).width(50).build());
    }

    @Override
    protected void addContents() {
        List<Button> buttons = new ArrayList<>();
        for(Scene scene : chapter.getSceneList()) {
            Button button = Button.builder(Component.literal(String.valueOf(scene.getName())), button1 -> {
            }).build();
            buttons.add(button);
        }
        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, buttons));
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
}
