package fr.loudo.narrativecraft.scenes;

import fr.loudo.narrativecraft.animations.Animation;
import fr.loudo.narrativecraft.story.Chapter;

import java.util.ArrayList;
import java.util.List;

public class Scene {

    private Chapter chapter;
    private List<Animation> animations;
    private String name;

    public Scene(Chapter chapter, String name) {
        this.chapter = chapter;
        this.animations = new ArrayList<>();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public List<Animation> getAnimations() {
        return animations;
    }

    public boolean addAnimation(Animation newAnimation) {
        if(animations.contains(newAnimation)) return false;
        animations.add(newAnimation);
        return true;
    }

    public Animation getAnimationByName(String animationName) {
        for(Animation animation : animations) {
            if(animation.getName().equalsIgnoreCase(animationName)) {
                return animation;
            }
        }
        return null;
    }

    public boolean animationExists(String animationName) {
        for(Animation animation : animations) {
            if(animation.getName().equalsIgnoreCase(animationName)) {
                return true;
            }
        }
        return false;
    }
}
