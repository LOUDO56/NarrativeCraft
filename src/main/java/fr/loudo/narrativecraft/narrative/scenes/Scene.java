package fr.loudo.narrativecraft.narrative.scenes;

import fr.loudo.narrativecraft.narrative.chapter.Chapter;

public class Scene {

    private transient Chapter chapter;
    private String name;

    public Scene(Chapter chapter, String name) {
        this.chapter = chapter;
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

}
