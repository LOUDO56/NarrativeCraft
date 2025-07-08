package fr.loudo.narrativecraft.narrative.chapter.scenes.animations;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.Cutscene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.recordings.actions.ActionsData;
import fr.loudo.narrativecraft.screens.storyManager.scenes.animations.AnimationsScreen;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;

public class Animation extends NarrativeEntry {

    private transient Scene scene;
    private CharacterStory character;
    private List<ActionsData> actionsData;
    private String skinName = "main.png";

    public Animation(Scene scene, String name, String description) {
        super(name, description);
        this.scene = scene;
        this.actionsData = new ArrayList<>();
    }

    public Animation(String name, String description) {
        super(name, description);
        this.actionsData = new ArrayList<>();
    }

    @Override
    public void update(String name, String description) {
        NarrativeCraftFile.removeAnimationFileFromScene(this);
        String oldName = this.name;
        String oldDescription = this.description;
        this.name = name;
        this.description = description;
        if(!NarrativeCraftFile.updateAnimationFile(this)) {
            this.name = oldName;
            this.description = oldDescription;
            ScreenUtils.sendToast(Translation.message("global.error"), Translation.message("screen.animation_manager.update.failed", name));
            return;
        }
        ScreenUtils.sendToast(Translation.message("global.info"), Translation.message("toast.description.updated"));
        Minecraft.getInstance().setScreen(reloadScreen());
    }

    public CharacterStory getCharacter() {
        return character;
    }

    public void setCharacter(CharacterStory character) {
        this.character = character;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public List<ActionsData> getActionsData() {
        return actionsData;
    }

    public void setActionsData(List<ActionsData> actionsData) {
        this.actionsData = actionsData;
    }

    public String getSkinName() {
        return skinName;
    }

    public void setSkinName(String skinName) {
        this.skinName = skinName;
    }

    @Override
    public void remove() {
        scene.removeAnimation(this);
        for(Cutscene cutscene : scene.getCutsceneList()) {
            cutscene.getAnimationList().removeIf(animation -> animation.getName().equals(name));
        }
        for(Subscene subscene : scene.getSubsceneList()) {
            subscene.getAnimationList().removeIf(animation -> animation.getName().equals(name));
        }
        NarrativeCraftFile.removeAnimationFileFromScene(this);
        NarrativeCraftFile.updateCutsceneFile(scene);
        NarrativeCraftFile.updateSubsceneFile(scene);
    }

    @Override
    public Screen reloadScreen() {
        return new AnimationsScreen(scene);
    }
}
