package fr.loudo.narrativecraft.narrative.chapter;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.screens.story_manager.StoryDetails;

import java.util.ArrayList;
import java.util.List;

public class Chapter extends StoryDetails {

    private int index;
    private List<Scene> sceneList;

    public Chapter(int index) {
        super("", "");
        this.index = index;
        this.sceneList = new ArrayList<>();
    }

    public Chapter(int index, String name, String description) {
        super(name, description);
        this.index = index;
        this.sceneList = new ArrayList<>();
    }

    public boolean addScene(Scene scene) {
        if(NarrativeCraftFile.createSceneFolder(scene)) {
            sceneList.add(scene);
            return true;
        }
        return false;
    }

    public boolean sceneExists(String name) {
        for(Scene scene : sceneList) {
            if(scene.getName().toLowerCase().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<Scene> getSceneList() {
        return sceneList;
    }
}
