package fr.loudo.narrativecraft.narrative.chapter.scenes;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.Cutscene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;
import fr.loudo.narrativecraft.screens.story_manager.StoryDetails;

import java.util.ArrayList;
import java.util.List;

public class Scene extends StoryDetails {

    private Chapter chapter;
    private List<Animation> animationList;
    private List<Cutscene> cutsceneList;
    private List<Subscene> subsceneList;

    public Scene(String name, String description, Chapter chapter) {
        super(name, description);
        this.chapter = chapter;
        this.animationList = new ArrayList<>();
        this.cutsceneList = new ArrayList<>();
        this.subsceneList = new ArrayList<>();
    }

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    public void addAnimation(Animation animation) {
        if(!animationList.contains(animation)) animationList.add(animation);
    }

    public boolean addCutscene(Cutscene cutscene) {
        cutsceneList.add(cutscene);
        if(!NarrativeCraftFile.updateCutsceneFile(this)) {
            cutsceneList.remove(cutscene);
            return false;
        }
        return true;
    }

    public boolean cutsceneExists(String name) {
        for (Cutscene cutscene : cutsceneList) {
            if(cutscene.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void addSubscene(Subscene subscene) {
        if(!subsceneList.contains(subscene)) subsceneList.add(subscene);
    }

    public List<Animation> getAnimationList() {
        return animationList;
    }

    public List<Cutscene> getCutsceneList() {
        return cutsceneList;
    }

    public List<Subscene> getSubsceneList() {
        return subsceneList;
    }

    public void setAnimationList(List<Animation> animationList) {
        this.animationList = animationList;
    }

    public void setCutsceneList(List<Cutscene> cutsceneList) {
        this.cutsceneList = cutsceneList;
    }

    public void setSubsceneList(List<Subscene> subsceneList) {
        this.subsceneList = subsceneList;
    }
}
