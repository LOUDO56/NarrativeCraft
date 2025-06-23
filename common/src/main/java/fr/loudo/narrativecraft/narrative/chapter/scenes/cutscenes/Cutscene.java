package fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeGroup;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeTrigger;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;
import fr.loudo.narrativecraft.screens.storyManager.scenes.cutscenes.CutscenesScreen;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;

public class Cutscene extends NarrativeEntry {

    private transient Scene scene;
    private transient List<Animation> animationList;
    private List<KeyframeGroup> keyframeGroupList;
    private List<KeyframeTrigger> keyframeTriggerList;
    private List<Subscene> subsceneList;
    private List<String> animationListString;

    public Cutscene(Scene scene, String name, String description) {
        super(name, description);
        this.scene = scene;
        this.keyframeGroupList = new ArrayList<>();
        this.subsceneList = new ArrayList<>();
        this.animationList = new ArrayList<>();
        this.animationListString = new ArrayList<>();
        this.keyframeTriggerList = new ArrayList<>();
    }

    public List<KeyframeGroup> getKeyframeGroupList() {
        return keyframeGroupList;
    }

    public void setKeyframePathList(List<KeyframeGroup> keyframeGroupList) {
        this.keyframeGroupList = keyframeGroupList;
    }

    public List<Subscene> getSubsceneList() {
        return subsceneList;
    }

    public void setSubsceneList(List<Subscene> subsceneList) {
        this.subsceneList = subsceneList;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public List<String> getAnimationListString() {
        return animationListString;
    }

    public void setAnimationListString(List<String> animationListString) {
        this.animationListString = animationListString;
    }

    public List<Animation> getAnimationList() {
        return animationList;
    }

    public void setAnimationList(List<Animation> animationList) {
        this.animationList = animationList;
    }

    public List<KeyframeTrigger> getKeyframeTriggerList() {
        return keyframeTriggerList;
    }

    public void setKeyframeTriggerList(List<KeyframeTrigger> keyframeTriggerList) {
        this.keyframeTriggerList = keyframeTriggerList;
    }

    @Override
    public void update(String name, String description) {
        String oldName = this.name;
        String oldDescription = this.description;
        this.name = name;
        this.description = description;
        if(!NarrativeCraftFile.updateCutsceneFile(scene)) {
            this.name = oldName;
            this.description = oldDescription;
            ScreenUtils.sendToast(Translation.message("global.error"), Translation.message("screen.cutscene_manager.update.failed", name));
            return;
        }
        ScreenUtils.sendToast(Translation.message("global.info"), Translation.message("toast.description.updated"));
        Minecraft.getInstance().setScreen(reloadScreen());
    }

    @Override
    public void remove() {
        scene.removeCutscene(this);
        NarrativeCraftFile.updateCutsceneFile(scene);
    }

    @Override
    public Screen reloadScreen() {
        return new CutscenesScreen(scene);
    }
}
