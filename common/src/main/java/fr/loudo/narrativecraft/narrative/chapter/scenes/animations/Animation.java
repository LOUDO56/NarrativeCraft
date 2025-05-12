package fr.loudo.narrativecraft.narrative.chapter.scenes.animations;

import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.recordings.actions.ActionsData;
import fr.loudo.narrativecraft.screens.story_manager.StoryDetails;

public class Animation extends StoryDetails {

    private transient Scene scene;
    private Character character;
    private ActionsData actionsData;

    public Animation(String name, String description) {
        super(name, description);
        this.actionsData = new ActionsData();
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
