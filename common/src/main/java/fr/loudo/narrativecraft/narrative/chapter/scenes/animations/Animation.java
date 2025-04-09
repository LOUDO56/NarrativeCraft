package fr.loudo.narrativecraft.narrative.chapter.scenes.animations;

import fr.loudo.narrativecraft.narrative.recordings.actions.ActionsData;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;

public class Animation {

    private transient Scene scene;
    private String sceneName; // For GSON deserialization
    private int chapterIndex; // For GSON deserialization
    private String name;
    private Character character;
    private ActionsData actionsData;

    public Animation(Scene scene, String name){
        this.scene = scene;
        this.sceneName = scene.getName();
        this.chapterIndex = scene.getChapter().getIndex();
        this.name = name;
        this.actionsData = new ActionsData();
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

    public ActionsData getActionsData() {
        return actionsData;
    }

    public void setActionsData(ActionsData actionsData) {
        this.actionsData = actionsData;
    }

    public void setCharacter(Character character) {
        this.character = character;
    }
}
