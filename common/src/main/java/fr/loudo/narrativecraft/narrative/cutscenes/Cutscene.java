package fr.loudo.narrativecraft.narrative.cutscenes;

import fr.loudo.narrativecraft.narrative.cutscenes.keyframes.KeyframePath;
import fr.loudo.narrativecraft.narrative.scenes.Scene;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;

import java.util.ArrayList;
import java.util.List;

public class Cutscene {

    private transient Scene scene;
    private String name;
    private int chapterIndex;
    private String sceneName;
    private List<KeyframePath> keyframePathList;
    private List<TriggerCutscene> triggerCutsceneList;
    private List<Subscene> subsceneList;
    private Subscene defaultSubcene;

    public Cutscene(String name, Scene scene) {
        this.name = name;
        this.scene = scene;
        this.chapterIndex = scene.getChapter().getIndex();
        this.sceneName = scene.getName();
        this.keyframePathList = new ArrayList<>();
        this.triggerCutsceneList = new ArrayList<>();
        this.subsceneList = new ArrayList<>();
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getChapterIndex() {
        return chapterIndex;
    }

    public void setChapterIndex(int chapterIndex) {
        this.chapterIndex = chapterIndex;
    }

    public String getSceneName() {
        return sceneName;
    }

    public void setSceneName(String sceneName) {
        this.sceneName = sceneName;
    }

    public List<KeyframePath> getKeyframePathList() {
        return keyframePathList;
    }

    public void setKeyframePathList(List<KeyframePath> keyframePathList) {
        this.keyframePathList = keyframePathList;
    }

    public List<TriggerCutscene> getTriggerCutsceneList() {
        return triggerCutsceneList;
    }

    public void setTriggerCutsceneList(List<TriggerCutscene> triggerCutsceneList) {
        this.triggerCutsceneList = triggerCutsceneList;
    }

    public List<Subscene> getSubsceneList() {
        return subsceneList;
    }

    public void setSubsceneList(List<Subscene> subsceneList) {
        this.subsceneList = subsceneList;
    }

    public Subscene getDefaultSubcene() {
        return defaultSubcene;
    }

    public void setDefaultSubcene(Subscene defaultSubcene) {
        this.defaultSubcene = defaultSubcene;
    }
}
