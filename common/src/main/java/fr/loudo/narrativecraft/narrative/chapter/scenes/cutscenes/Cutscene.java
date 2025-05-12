package fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes;

import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeGroup;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;
import fr.loudo.narrativecraft.screens.story_manager.StoryDetails;

import java.util.ArrayList;
import java.util.List;

public class Cutscene extends StoryDetails {

    private transient Scene scene;
    private List<KeyframeGroup> keyframeGroupList;
    private List<Subscene> subsceneList;
    //private Subscene defaultSubcene;

    public Cutscene(Scene scene, String name, String description) {
        super(name, description);
        this.scene = scene;
        this.keyframeGroupList = new ArrayList<>();
        this.subsceneList = new ArrayList<>();
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
}
