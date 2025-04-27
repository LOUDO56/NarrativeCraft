package fr.loudo.narrativecraft.screens;

import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.CreditsAndAttributionScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.SkinCustomizationScreen;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import net.minecraft.client.gui.screens.options.controls.ControlsScreen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.CommonLinks;

import java.util.List;

public class CutsceneSettingsScreen extends OptionsSubScreen {

    private Screen lastScreen;
    private CutsceneController cutsceneController;
    private final int BUTTON_WIDTH = 400;
    private final int BUTTON_HEIGHT = 20;

    public CutsceneSettingsScreen(CutsceneController cutsceneController, Screen lastScreen) {
        super(lastScreen, Minecraft.getInstance().options, Component.literal("Cutscene Settings"));
        this.lastScreen = lastScreen;
        this.cutsceneController = cutsceneController;
    }

    @Override
    protected void addContents() {
        TestOptionList optionList = new TestOptionList(this.minecraft, this.width, this);
        LayoutElement element = this.layout.addToContents(optionList);
        for (int i = 0; i < 40; i++) {
            optionList.addButton(Button.builder(Component.literal("aaa"), button -> {}).pos(0, 0).build());
        }
    }

    @Override
    protected void addOptions() {

    }


    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
