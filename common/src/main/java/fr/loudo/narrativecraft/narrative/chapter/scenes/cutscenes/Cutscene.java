package fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeGroup;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Cutscene {

    private transient Scene scene;
    private String name;
    private int chapterIndex;
    private String sceneName;
    private List<KeyframeGroup> keyframeGroupList;
    private List<Subscene> subsceneList;
    //private Subscene defaultSubcene;

    public Cutscene(String name, Scene scene) {
        this.name = name;
        this.scene = scene;
        this.chapterIndex = scene.getChapter().getIndex();
        this.sceneName = scene.getName();
        this.keyframeGroupList = new ArrayList<>();
        this.subsceneList = new ArrayList<>();
    }

    public boolean addSubscene(Subscene subscene) {
        if(subsceneList.contains(subscene)) return false;
        try {
            subsceneList.add(subscene);
            NarrativeCraftFile.saveCutscene(this);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public boolean removeSubscene(Subscene subscene) {
        if(!subsceneList.contains(subscene)) return false;
        try {
            subsceneList.remove(subscene);
            NarrativeCraftFile.saveCutscene(this);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public boolean subsceneExists(String subsceneName) {
        for(Subscene subscene : subsceneList) {
            if(subscene.getName().equalsIgnoreCase(subsceneName)) {
                return true;
            }
        }
        return false;
    }

    public Subscene getSubsceneByName(String subsceneName) {
        for(Subscene subscene : subsceneList) {
            if(subscene.getName().equalsIgnoreCase(subsceneName)) {
                return subscene;
            }
        }
        return null;
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

}
