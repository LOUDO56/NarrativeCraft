package fr.loudo.narrativecraft.narrative.chapter.scenes;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleGroup;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.Cutscene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.screens.storyManager.scenes.ScenesScreen;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;

public class Scene extends NarrativeEntry {

    private Chapter chapter;
    private int placement;
    private transient List<CharacterStory> npcs;
    private List<Animation> animationList;
    private List<Cutscene> cutsceneList;
    private List<Subscene> subsceneList;
    private List<CameraAngleGroup> cameraAngleGroupList;

    public Scene(String name, String description, Chapter chapter) {
        super(name, description);
        this.chapter = chapter;
        this.animationList = new ArrayList<>();
        this.cutsceneList = new ArrayList<>();
        this.subsceneList = new ArrayList<>();
        this.cameraAngleGroupList = new ArrayList<>();
        this.npcs = new ArrayList<>();
        placement = chapter.getSceneList().size() + 1;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    public int getPlacement() {
        return placement;
    }

    public void setPlacement(int placement) {
        this.placement = placement;
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

    public boolean addSubscene(Subscene subscene) {
        subsceneList.add(subscene);
        if(!NarrativeCraftFile.updateSubsceneFile(this)) {
            subsceneList.remove(subscene);
            return false;
        }
        return true;
    }

    public boolean addCameraAnglesGroup(CameraAngleGroup cameraAngleGroup) {
        cameraAngleGroupList.add(cameraAngleGroup);
        if(!NarrativeCraftFile.updateCameraAnglesFile(this)) {
            cameraAngleGroupList.remove(cameraAngleGroup);
            return false;
        }
        return true;
    }

    public boolean cutsceneExists(String name) {
        for (Cutscene cutscene : cutsceneList) {
            if(cutscene.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean subsceneExists(String name) {
        for (Subscene subscene : subsceneList) {
            if(subscene.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean cameraAnglesGroupExists(String name) {
        for (CameraAngleGroup cameraAngleGroup : cameraAngleGroupList) {
            if(cameraAngleGroup.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public Animation getAnimationByName(String name) {
        for(Animation animation : animationList) {
            if(animation.getName().equalsIgnoreCase(name)) {
                return animation;
            }
        }
        return null;
    }

    public Subscene getSubsceneByName(String name) {
        for(Subscene subscene : subsceneList) {
            if(subscene.getName().equalsIgnoreCase(name)) {
                return subscene;
            }
        }
        return null;
    }

    public Cutscene getCutsceneByName(String name) {
        for(Cutscene cutscene : cutsceneList) {
            if(cutscene.getName().equalsIgnoreCase(name)) {
                return cutscene;
            }
        }
        return null;
    }

    public CameraAngleGroup getCameraAnglesGroupByName(String name) {
        for(CameraAngleGroup cameraAngleGroup : cameraAngleGroupList) {
            if(cameraAngleGroup.getName().equalsIgnoreCase(name)) {
                return cameraAngleGroup;
            }
        }
        return null;
    }

    public void removeAnimation(Animation animation) {
        animationList.remove(animation);
    }

    public void removeCutscene(Cutscene cutscene) {
        cutsceneList.remove(cutscene);
    }

    public void removeSubscene(Subscene subscene) {
        subsceneList.remove(subscene);
    }

    public void removeCameraAnglesGroup(CameraAngleGroup cameraAngleGroup) {
        cameraAngleGroupList.remove(cameraAngleGroup);
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

    public List<CameraAngleGroup> getCameraAngleGroupList() {
        return cameraAngleGroupList;
    }

    public void setCameraAngleGroupList(List<CameraAngleGroup> cameraAngleGroupList) {
        this.cameraAngleGroupList = cameraAngleGroupList;
    }

    public String getSnakeCase() {
        return String.join("_", name.toLowerCase().split(" "));
    }

    public List<CharacterStory> getNpcs() {
        return npcs;
    }

    public void setNpcs(List<CharacterStory> npcs) {
        this.npcs = npcs;
    }

    public void addNpc(CharacterStory characterStory) {
        npcs.add(characterStory);
    }

    public void removeNpc(CharacterStory characterStory) {
        npcs.remove(characterStory);
    }

    public boolean npcExists(String name) {
        for(CharacterStory characterStory : npcs) {
            if(characterStory.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public CharacterStory getNpc(String name) {
        for(CharacterStory characterStory : npcs) {
            if(characterStory.getName().equalsIgnoreCase(name)) {
                return characterStory;
            }
        }
        return null;
    }

    @Override
    public void update(String name, String description) {}

    public void update(String name, String description, int placement) {
        if(!NarrativeCraftFile.updateSceneDetails(this, name, description, placement)) {
            ScreenUtils.sendToast(Translation.message("global.error"), Translation.message("screen.scene_manager.update.failed", name));
            return;
        }
        String oldName = this.name;
        this.name = name;
        this.description = description;
        for(Scene scene : chapter.getSceneList()) {
            if(scene.getPlacement() == placement) {
                scene.setPlacement(this.placement);
                NarrativeCraftFile.updateSceneDetails(scene, scene.getName(), scene.getDescription(), scene.getPlacement());
            }
        }
        this.placement = placement;
        ScreenUtils.sendToast(Translation.message("global.info"), Translation.message("toast.description.updated", name, chapter.getIndex()));
        Minecraft.getInstance().setScreen(reloadScreen());
        NarrativeCraftFile.updateMainInkFile();
        NarrativeCraftFile.updateKnotSceneNameFromChapter(chapter, oldName, name);
    }


    @Override
    public void remove() {
        chapter.removeScene(this);
        NarrativeCraftFile.removeSceneFolder(this);
        NarrativeCraftFile.updateMainInkFile();
    }

    @Override
    public Screen reloadScreen() {
        return new ScenesScreen(chapter);
    }
}
