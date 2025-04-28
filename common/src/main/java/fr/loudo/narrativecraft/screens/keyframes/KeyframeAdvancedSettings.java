package fr.loudo.narrativecraft.screens.keyframes;

import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.Keyframe;
import fr.loudo.narrativecraft.screens.cutscenes.CutsceneSettingsList;
import fr.loudo.narrativecraft.screens.cutscenes.CutsceneSettingsScreen;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;

public class KeyframeAdvancedSettings extends CutsceneSettingsScreen {

    private final Keyframe keyframe;

    public KeyframeAdvancedSettings(CutsceneController cutsceneController, Screen lastScreen, Keyframe keyframe) {
        super(cutsceneController, lastScreen, Translation.message("screen.keyframe_advanced.name"));
        this.keyframe = keyframe;
    }

    @Override
    protected void addContents() {
        this.cutsceneSettingsList = new CutsceneSettingsList(this.minecraft, this.width, this);

        Button easingsButton = Button.builder(Translation.message("screen.keyframe_advanced.easings"), button -> {
            KeyframeEasingsScreen screen = new KeyframeEasingsScreen(this.lastScreen, this.options, keyframe);
            this.minecraft.setScreen(screen);
        }).build();

        cutsceneSettingsList.addButton(easingsButton);

        cutsceneSettingsList = this.layout.addToContents(cutsceneSettingsList);

    }
}
