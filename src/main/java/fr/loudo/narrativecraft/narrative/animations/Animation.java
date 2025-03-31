package fr.loudo.narrativecraft.narrative.animations;

import fr.loudo.narrativecraft.narrative.scenes.Scene;
import fr.loudo.narrativecraft.utils.Location;

import java.util.ArrayList;
import java.util.List;

public class Animation {

    private transient Scene scene;
    private String sceneName; // For GSON deserialization
    private int chapterIndex; // For GSON deserialization
    private String name;
    private Character character;
    private List<Location> locations;

    public Animation(Scene scene, String name){
        this.scene = scene;
        this.sceneName = scene.getName();
        this.chapterIndex = scene.getChapter().getIndex();
        this.name = name;
        this.locations = new ArrayList<>();
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public String getSceneName() {
        return sceneName;
    }

    public int getChapterIndex() {
        return chapterIndex;
    }

    public String getName() {
        return name;
    }

    public Character getCharacter() {
        return character;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    public void setCharacter(Character character) {
        this.character = character;
    }
}
