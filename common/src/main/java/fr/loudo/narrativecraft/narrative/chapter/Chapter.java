package fr.loudo.narrativecraft.narrative.chapter;

import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;

import java.util.ArrayList;
import java.util.List;

public class Chapter {

    private int index;
    private String name, description;
    private List<Scene> sceneList;

    public Chapter(int index) {
        this.index = index;
        this.sceneList = new ArrayList<>();
    }

    public void addScene(Scene scene) {
        if(!sceneList.contains(scene)) sceneList.add(scene);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Scene> getSceneList() {
        return sceneList;
    }
}
