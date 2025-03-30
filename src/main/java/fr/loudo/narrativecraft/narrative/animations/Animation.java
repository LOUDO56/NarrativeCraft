package fr.loudo.narrativecraft.narrative.animations;

import fr.loudo.narrativecraft.narrative.scenes.Scene;

public class Animation {

    private transient Scene scene;
    private String name;
    private Character character;

    public Animation(Scene scene, String name){
        this.scene = scene;
        this.name = name;
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

    public Character getCharacter() {
        return character;
    }

    public void setCharacter(Character character) {
        this.character = character;
    }
}
