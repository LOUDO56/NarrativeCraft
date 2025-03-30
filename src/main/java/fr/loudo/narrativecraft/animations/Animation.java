package fr.loudo.narrativecraft.animations;

import com.bladecoder.ink.runtime.Story;
import fr.loudo.narrativecraft.scenes.Scene;

public class Animation {

    private Scene scene;
    private String name;

    public Animation(Scene scene, String name){
        this.scene = scene;
        this.name = name;
    }

    public Scene getScene() {
        return scene;
    }

    public String getName() {
        return name;
    }
}
