package fr.loudo.narrativecraft.screens.cutscenes;

import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.screens.components.ObjectListScreen;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

public class CutsceneSettingsScreen extends OptionsSubScreen {

    protected Screen lastScreen;
    protected CutsceneController cutsceneController;
    protected ObjectListScreen objectListScreen;

    public CutsceneSettingsScreen(CutsceneController cutsceneController, Screen lastScreen, Component title) {
        super(lastScreen, Minecraft.getInstance().options, title);
        this.lastScreen = lastScreen;
        this.cutsceneController = cutsceneController;
    }

    @Override
    protected void addContents() {

        this.objectListScreen = new ObjectListScreen(this.minecraft, this.width, this);

        Button changeTimeSkip = Button.builder(Translation.message("screen.cutscenes_settings.time_skip"), button -> {
            CutsceneChangeTimeSkipScreen screen = new CutsceneChangeTimeSkipScreen(cutsceneController, this);
            this.minecraft.setScreen(screen);
        }).build();

        Button selectKeyframeGroup = Button.builder(Translation.message("screen.cutscenes_settings.select_group"), button -> {
            CutsceneSelectKeyframeGroupScreen screen = new CutsceneSelectKeyframeGroupScreen(cutsceneController, this);
            this.minecraft.setScreen(screen);
        }).build();

        objectListScreen.addButton(changeTimeSkip);
        objectListScreen.addButton(selectKeyframeGroup);
        objectListScreen = this.layout.addToContents(objectListScreen);

    }

    @Override
    protected void addOptions() {

    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        if (this.objectListScreen != null) {
            this.objectListScreen.updateSize(this.width, this.layout);
        }

    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
