package fr.loudo.narrativecraft.narrative.animations;

import fr.loudo.narrativecraft.narrative.scenes.Scene;

public class Animation {

    private transient Scene scene;
    private String sceneName;
    private String name;
    private Character character;

    public Animation(Scene scene, String name){
        this.scene = scene;
        this.sceneName = scene.getName();
        this.name = name;
    }

    public Scene getScene() {
        return scene;
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
