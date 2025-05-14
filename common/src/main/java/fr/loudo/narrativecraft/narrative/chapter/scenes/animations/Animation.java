package fr.loudo.narrativecraft.narrative.chapter.scenes.animations;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.recordings.actions.ActionsData;
import fr.loudo.narrativecraft.narrative.StoryDetails;

public class Animation extends StoryDetails {

    private transient Scene scene;
    private Character character;
    private ActionsData actionsData;

    public Animation(Scene scene, String name, String description) {
        super(name, description);
        this.scene = scene;
        this.actionsData = new ActionsData();
    }

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

    @Override
    public void remove() {
        scene.removeAnimation(this);
        NarrativeCraftFile.removeAnimationFileFromScene(this);
    }
}
