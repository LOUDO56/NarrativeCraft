package fr.loudo.narrativecraft.narrative.scenes;

import fr.loudo.narrativecraft.NarrativeCraft;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Scene {

    private transient Chapter chapter;
    private List<String> animationFilesName;
    private String name;

    public Scene(Chapter chapter, String name) {
        this.chapter = chapter;
        this.animationFilesName = new ArrayList<>();
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

    public List<String> getAnimationFilesName() {
        return animationFilesName;
    }

    public boolean addAnimation(String animationName) {
        if(animationFilesName.contains(animationName)) return false;
        animationFilesName.add(animationName);
        return true;
    }

    public boolean removeAnimation(String animationName) {
        if(!animationFilesName.contains(animationName)) return false;
        try {
            NarrativeCraftFile.removeAnimationFile(animationName);
            animationFilesName.remove(animationName);
            NarrativeCraftFile.saveChapter(chapter);
            return true;
        } catch (IOException e) {
            NarrativeCraft.LOGGER.warn("Couldn't remove animation " + animationName + " file: " + e);
            return false;
        }
    }
}
