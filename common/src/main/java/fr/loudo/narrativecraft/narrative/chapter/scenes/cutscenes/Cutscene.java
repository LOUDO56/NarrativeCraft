package fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.StoryDetails;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeGroup;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;

import java.util.ArrayList;
import java.util.List;

public class Cutscene extends StoryDetails {

    private transient Scene scene;
    private transient List<Animation> animationList;
    private List<KeyframeGroup> keyframeGroupList;
    private List<Subscene> subsceneList;
    private List<String> animationListString;
    //private Subscene defaultSubcene;

    public Cutscene(Scene scene, String name, String description) {
        super(name, description);
        this.scene = scene;
        this.keyframeGroupList = new ArrayList<>();
        this.subsceneList = new ArrayList<>();
        this.animationList = new ArrayList<>();
        this.animationListString = new ArrayList<>();
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

    @Override
    public void remove() {
        scene.removeCutscene(this);
        NarrativeCraftFile.updateCutsceneFile(scene);
    }
}
