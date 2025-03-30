package fr.loudo.narrativecraft.narrative.scenes;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Scene {

    private transient Chapter chapter;
    private String name;
    private List<Animation> animations;

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

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    public List<Animation> getAnimations() {
        return animations;
    }

    public boolean addAnimation(Animation newAnimation) {
        if(animations.contains(newAnimation)) return false;;
        try {
            animations.add(newAnimation);
            NarrativeCraftFile.saveChapter(chapter);
            return true;
        } catch (IOException e) {
            throw new RuntimeException("Couldn't save chapter " + chapter.getIndex() + " file: " + e);
        }
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
