package fr.loudo.narrativecraft.narrative.scenes;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.cutscenes.Cutscene;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import net.minecraft.commands.CommandSourceStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Scene {

    private transient Chapter chapter;
    private List<String> animationFilesName;
    private List<String> cutsceneFilesName;
    private List<Subscene> subsceneList;
    private String name;

    public Scene(Chapter chapter, String name) {
        this.chapter = chapter;
        this.animationFilesName = new ArrayList<>();
        this.cutsceneFilesName = new ArrayList<>();
        this.subsceneList = new ArrayList<>();
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
            NarrativeCraftMod.LOG.warn("Couldn't remove animation " + animationName + " file: " + e);
            return false;
        }
    }
    
    public boolean addSubscene(Subscene subscene) {
        if(subsceneList.contains(subscene)) return false;
        try {
            subsceneList.add(subscene);
            NarrativeCraftFile.saveChapter(chapter);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public SuggestionProvider<CommandSourceStack> getSubscenesSuggestion() {
        return (context, builder) -> {
            for (Subscene subscene : subsceneList) {
                builder.suggest(subscene.getName());
            }
            return builder.buildFuture();
        };
    }

    public boolean removeSubscene(Subscene subscene) {
        if(!subsceneList.contains(subscene)) return false;
        try {
            subsceneList.remove(subscene);
            NarrativeCraftFile.saveChapter(chapter);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public Subscene getSubsceneByName(String subsceneName) {
        for(Subscene subscene : subsceneList) {
            if(subscene.getName().equalsIgnoreCase(subsceneName)) {
                return subscene;
            }
        }
        return null;
    }

    public boolean subsceneExists(String subsceneName) {
        for(Subscene subscene : subsceneList) {
            if(subscene.getName().equalsIgnoreCase(subsceneName)) {
                return true;
            }
        }
        return false;
    }

    public List<Subscene> getSubsceneList() {
        return subsceneList;
    }

    public void setSubsceneList(List<Subscene> subsceneList) {
        this.subsceneList = subsceneList;
    }

    public List<String> getCutsceneFilesName() {
        return cutsceneFilesName;
    }

    public boolean addCutscene(String cutsceneName) {
        String cutsceneFileName = NarrativeCraftFile.getFileNameTemplate(chapter.getIndex(), name, cutsceneName);
        if(cutsceneFilesName.contains(cutsceneFileName)) return false;
        try {
            Cutscene cutscene = new Cutscene(cutsceneName, this);
            cutsceneFilesName.add(cutsceneFileName);
            NarrativeCraftFile.saveCutscene(cutscene);
            NarrativeCraftFile.saveChapter(chapter);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public boolean removeCutscene(String cutsceneName) {
        String cutsceneFileName = NarrativeCraftFile.getFileNameTemplate(chapter.getIndex(), name, cutsceneName);
        if(!cutsceneFilesName.contains(cutsceneFileName)) return false;
        try {
            cutsceneFilesName.remove(cutsceneFileName);
            NarrativeCraftFile.removeCutsceneFile(cutsceneFileName);
            NarrativeCraftFile.saveChapter(chapter);
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
