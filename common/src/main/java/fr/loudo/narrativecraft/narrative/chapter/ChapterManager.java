package fr.loudo.narrativecraft.narrative.chapter;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.Cutscene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the chapters in the narrative.
 */
public class ChapterManager {

    private final List<Chapter> chapters;

    /**
     * Initializes the ChapterManager with an empty list of chapters.
     */
    public ChapterManager() {
        this.chapters = new ArrayList<>();
        init();
    }

    private void init() {
        File chapterDirectory = NarrativeCraftFile.chaptersDirectory;
        File[] chapterIndexFolder = chapterDirectory.listFiles();
        if(chapterIndexFolder != null) {
            for(File chapterIndex : chapterIndexFolder) {
                int index = Integer.parseInt(chapterIndex.getName());
                Chapter chapter = new Chapter(index);
                String name = "";
                String desc = "";
                File detailsFile = NarrativeCraftFile.getDetailsFile(chapterIndex);
                if(detailsFile.exists()) {
                    try {
                        String content = Files.readString(detailsFile.toPath());
                        JsonObject json = JsonParser.parseString(content).getAsJsonObject();
                        name = json.get("name").getAsString();
                        desc = json.get("description").getAsString();
                    } catch (IOException e) {
                        NarrativeCraftMod.LOG.warn("Settings file of chapter {} does not exists, passing...", index);
                    }
                }
                chapter.setName(name);
                chapter.setDescription(desc);
                initScenesOfChapter(chapterIndex, chapter);
                chapters.add(chapter);
            }
        }
    }

    private void initScenesOfChapter(File chapterIndex, Chapter chapter) {
        File scenesDirectory = new File(chapterIndex.getAbsoluteFile(), "scenes");
        if(scenesDirectory.exists()) {
            File[] scenesFolder = scenesDirectory.listFiles();
            if(scenesFolder != null) {
                for(File sceneFolder : scenesFolder) {
                    File dataFolder = new File(sceneFolder.getAbsoluteFile(), "data");
                    String name = sceneFolder.getName();
                    String desc = "";
                    Scene scene = new Scene(name, desc, chapter);
                    scene.setChapter(chapter);
                    if(dataFolder.exists()) {
                        File detailsFile = NarrativeCraftFile.getDetailsFile(dataFolder);
                        if(detailsFile.exists()) {
                            try {
                                String content = Files.readString(detailsFile.toPath());
                                JsonObject json = JsonParser.parseString(content).getAsJsonObject();
                                name = json.get("name").getAsString();
                                desc = json.get("description").getAsString();
                                scene.setName(name);
                                scene.setDescription(desc);
                            } catch (IOException e) {
                                NarrativeCraftMod.LOG.warn("Settings file of scene {} of chapter {} does not exists, passing...", sceneFolder.getName(), chapterIndex.getName());
                            }
                        }
                        initSceneData(sceneFolder, scene);
                    }
                    chapter.addScene(scene);
                }
            }
        }
    }

    private void initSceneData(File sceneFolder, Scene scene) {
        // Animations
        File dataFolder = new File(sceneFolder, "data");
        File animationsFolder = new File(dataFolder, "animations");
        if(animationsFolder.exists()) {
            File[] animationsFile = animationsFolder.listFiles();
            if(animationsFile != null) {
                for(File animationFile : animationsFile) {
                    try {
                        String content = Files.readString(animationFile.toPath());
                        Animation animation = new Gson().fromJson(content, Animation.class);
                        animation.setScene(scene);
                        scene.addAnimation(animation);
                    } catch (IOException e) {
                        NarrativeCraftMod.LOG.warn("Animation file {} does not exists, passing...", animationFile.getName());
                    }
                }
            }
        }

        // Cutscenes
        File cutsceneFile = new File(dataFolder, "cutscenes" + NarrativeCraftFile.EXTENSION_DATA_FILE);
        if(cutsceneFile.exists()) {
            try {
                String content = Files.readString(cutsceneFile.toPath());
                Type listType = new TypeToken<List<Cutscene>>() {}.getType();
                List<Cutscene> cutscenes = new Gson().fromJson(content, listType);
                if(cutscenes != null) {
                    for (Cutscene cutscene : cutscenes) {
                        cutscene.setScene(scene);
                    }
                    scene.setCutsceneList(cutscenes);
                }
            } catch (IOException e) {
                NarrativeCraftMod.LOG.warn("Cutscene file does not exists, passing...");
            }
        }

        // Subscenes
        File subsceneFile = new File(dataFolder, "subscenes" + NarrativeCraftFile.EXTENSION_DATA_FILE);
        if(subsceneFile.exists()) {
            try {
                String content = Files.readString(subsceneFile.toPath());
                Type listType = new TypeToken<List<Subscene>>() {}.getType();
                List<Subscene> subscenes = new Gson().fromJson(content, listType);
                if(subscenes != null) {
                    for (Subscene subscene : subscenes) {
                        subscene.setScene(scene);
                        subscene.setAnimationList(new ArrayList<>());
                        for(Animation animation : scene.getAnimationList()) {
                            if(subscene.getAnimationNameList().contains(animation.getName())) {
                                subscene.getAnimationList().add(animation);
                            }
                        }
                    }
                    scene.setSubsceneList(subscenes);
                }
            } catch (IOException e) {
                NarrativeCraftMod.LOG.warn("Subscene file does not exists, passing...");
            }
        }


    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public boolean addChapter(String name, String description) {
        Chapter chapter = new Chapter(chapters.size() + 1, name, description);
        if(NarrativeCraftFile.createChapterDirectory(chapter)) {
            chapters.add(chapter);
            return true;
        } else {
            return false;
        }
    }

    public void removeChapter(Chapter chapter) {
        chapters.remove(chapter);
    }
}

