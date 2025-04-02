package fr.loudo.narrativecraft.narrative.animations;

import fr.loudo.narrativecraft.narrative.scenes.Scene;
import fr.loudo.narrativecraft.narrative.recordings.MovementData;

import java.util.ArrayList;
import java.util.List;

public class Animation {

    private transient Scene scene;
    private String sceneName; // For GSON deserialization
    private int chapterIndex; // For GSON deserialization
    private String name;
    private Character character;
    private List<MovementData> movementData;

    public Animation(Scene scene, String name){
        this.scene = scene;
        this.sceneName = scene.getName();
        this.chapterIndex = scene.getChapter().getIndex();
        this.name = name;
        this.movementData = new ArrayList<>();
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

    public List<MovementData> getLocations() {
        return movementData;
    }

    public void setLocations(List<MovementData> movementData) {
        this.movementData = movementData;
    }

    public void setCharacter(Character character) {
        this.character = character;
    }
}
