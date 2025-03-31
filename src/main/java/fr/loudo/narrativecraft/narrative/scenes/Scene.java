package fr.loudo.narrativecraft.narrative.scenes;

import fr.loudo.narrativecraft.narrative.chapter.Chapter;

import java.util.ArrayList;
import java.util.List;

public class Scene {

    private transient Chapter chapter;
    private List<String> animationFiles;
    private String name;

    public Scene(Chapter chapter, String name) {
        this.chapter = chapter;
        this.animationFiles = new ArrayList<>();
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

    public List<String> getAnimationFiles() {
        return animationFiles;
    }
}
