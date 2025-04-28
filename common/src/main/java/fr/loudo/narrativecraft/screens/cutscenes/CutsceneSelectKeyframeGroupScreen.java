package fr.loudo.narrativecraft.screens.cutscenes;

import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeGroup;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CutsceneSelectKeyframeGroupScreen extends CutsceneSettingsScreen {
    public CutsceneSelectKeyframeGroupScreen(CutsceneController cutsceneController, Screen lastScreen) {
        super(cutsceneController, lastScreen);
    }

    @Override
    protected void addContents() {
        this.cutsceneSettingsList = new CutsceneSettingsList(this.minecraft, this.width, this);
        for(KeyframeGroup keyframeGroup : this.cutsceneController.getCutscene().getKeyframeGroupList()) {
            Component name;
            if(cutsceneController.getSelectedKeyframeGroup().getId() == keyframeGroup.getId()) {
                name = Translation.message("screen.cutscenes_settings.select_group_name_selected", keyframeGroup.getId());
            } else {
                name = Translation.message("screen.cutscenes_settings.select_group_name", keyframeGroup.getId());
            }
            Button button = Button.builder(name, button1 -> {
                this.cutsceneController.setSelectedKeyframeGroup(keyframeGroup);
                this.onClose();
            }).build();
            this.cutsceneSettingsList.addButton(button);
        }
        cutsceneSettingsList = this.layout.addToContents(cutsceneSettingsList);

    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(null);
    }
}
