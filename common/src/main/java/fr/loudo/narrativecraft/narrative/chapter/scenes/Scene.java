package fr.loudo.narrativecraft.narrative.chapter.scenes;

import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.Cutscene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;
import fr.loudo.narrativecraft.screens.story_manager.StoryDetails;

import java.util.ArrayList;
import java.util.List;

public class Scene extends StoryDetails {

    private String name, description;
    private Chapter chapter;
    private final List<Animation> animationList;
    private final List<Cutscene> cutsceneList;
    private final List<Subscene> subsceneList;

    public Scene(String name, String description, Chapter chapter) {
        super(name, description);
        this.chapter = chapter;
        this.animationList = new ArrayList<>();
        this.cutsceneList = new ArrayList<>();
        this.subsceneList = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addAnimation(Animation animation) {
        if(!animationList.contains(animation)) animationList.add(animation);
    }

    public void addCutscene(Cutscene cutscene) {
        if(!cutsceneList.contains(cutscene)) cutsceneList.add(cutscene);
    }

    public void addSubscene(Subscene subscene) {
        if(!subsceneList.contains(subscene)) subsceneList.add(subscene);
    }
}
