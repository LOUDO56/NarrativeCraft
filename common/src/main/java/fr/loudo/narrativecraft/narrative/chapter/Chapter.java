package fr.loudo.narrativecraft.narrative.chapter;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.screens.storyManager.chapters.ChaptersScreen;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;

public class Chapter extends NarrativeEntry {

    private int index;
    private List<Scene> sceneList;

    public Chapter(int index) {
        super("", "");
        this.index = index;
        this.sceneList = new ArrayList<>();
    }

    public Chapter(int index, String name, String description) {
        super(name, description);
        this.index = index;
        this.sceneList = new ArrayList<>();
    }

    public boolean addScene(Scene scene) {
        if(NarrativeCraftFile.createSceneFolder(scene)) {
            sceneList.add(scene);
            NarrativeCraftFile.updateMainInkFile();
            return true;
        }
        return false;
    }

    public void removeScene(Scene scene) {
        sceneList.remove(scene);
    }

    public boolean sceneExists(String name) {
        for(Scene scene : sceneList) {
            if(scene.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public Scene getSceneByName(String sceneName) {
        for(Scene scene : sceneList) {
            if(scene.getName().equalsIgnoreCase(sceneName)) {
                return scene;
            }
        }
        return null;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<Scene> getSceneList() {
        return sceneList;
    }

    @Override
    public void update(String name, String description) {
        if(!NarrativeCraftFile.updateChapterDetails(this, name, description)) {
            ScreenUtils.sendToast(Translation.message("toast.error"), Translation.message("screen.chapter_manager.update.failed"));
            return;
        }
        this.name = name;
        this.description = description;
        ScreenUtils.sendToast(Translation.message("toast.info"), Translation.message("toast.description.updated", index));
        Minecraft.getInstance().setScreen(reloadScreen());
        NarrativeCraftFile.updateMainInkFile();
    }

    @Override
    public void remove() {
        NarrativeCraftFile.removeChapterFolder(this);
        NarrativeCraftMod.getInstance().getChapterManager().removeChapter(this);
        NarrativeCraftFile.updateMainInkFile();
    }

    @Override
    public Screen reloadScreen() {
        return new ChaptersScreen();
    }
}
