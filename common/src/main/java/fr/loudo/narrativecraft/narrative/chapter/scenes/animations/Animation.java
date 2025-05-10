package fr.loudo.narrativecraft.narrative.chapter.scenes.animations;

import fr.loudo.narrativecraft.narrative.recordings.actions.ActionsData;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;

import java.util.List;

public class Animation {

    private String name;
    private Character character;
    private Scene scene;
    private ActionsData actionsData;

    public Animation(String name, Scene scene) {
        this.name = name;
        this.scene = scene;
        this.actionsData = new ActionsData();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Character getCharacter() {
        return character;
    }

    public void setCharacter(Character character) {
        this.character = character;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public ActionsData getActionsData() {
        return actionsData;
    }

    public void setActionsData(ActionsData actionsData) {
        this.actionsData = actionsData;
    }
}
