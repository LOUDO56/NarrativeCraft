package fr.loudo.narrativecraft.narrative.chapter;

import fr.loudo.narrativecraft.NarrativeCraft;
import fr.loudo.narrativecraft.narrative.scenes.Scene;
import fr.loudo.narrativecraft.narrative.scenes.SceneManager;

import java.util.ArrayList;
import java.util.List;

public class Chapter {

    private final SceneManager sceneManager = NarrativeCraft.getSceneManager();

    private int index;
    private String name;
    private List<Scene> scenes;

    public Chapter(int index) {
        this.index = index;
        this.scenes = new ArrayList<>();
    }

    public Chapter(int index, String name) {
        this.index = index;
        this.name = name;
        this.scenes = new ArrayList<>();
    }


    public boolean addScene(Scene newScene) {
        if(scenes.contains(newScene)) return false;
        sceneManager.addScene(newScene);
        scenes.add(newScene);
        return true;
    }

    public List<Scene> getScenes() {
        return scenes;
    }

    public Scene getSceneByName(String sceneName) {
        for(Scene scene : scenes) {
            if(scene.getName().equalsIgnoreCase(sceneName)) {
                return scene;
            }
        }
        return null;
    }

    public boolean sceneExists(String sceneName) {
        for(Scene scene : scenes) {
            if(scene.getName().equalsIgnoreCase(sceneName)) {
                return true;
            }
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }
}
