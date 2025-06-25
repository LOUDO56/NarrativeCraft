package fr.loudo.narrativecraft.narrative.chapter;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleGroup;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.Cutscene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;
import fr.loudo.narrativecraft.narrative.character.CharacterSkinController;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import fr.loudo.narrativecraft.narrative.dialog.DialogData;
import fr.loudo.narrativecraft.narrative.recordings.actions.Action;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionDeserializer;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.commands.CommandSourceStack;

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

    private List<Chapter> chapters;

    public void init() {
        chapters = new ArrayList<>();
        DialogData.globalDialogData = NarrativeCraftFile.getGlobalDialogValues();
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
            NarrativeCraftFile.updateMainInkFile();
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
                    scene.setNpcs(new ArrayList<>());
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
        File dataFolder = new File(sceneFolder, "data");

        // Npcs
        File npcFolder = new File(dataFolder, "npc");
        if(npcFolder.exists()) {
            File[] characterList = npcFolder.listFiles();
            if(characterList != null) {
                for(File characterFolder : characterList) {
                    try {
                        File dataFile = new File(characterFolder, "data" + NarrativeCraftFile.EXTENSION_DATA_FILE);
                        String dataContent = Files.readString(dataFile.toPath());
                        CharacterStory characterStory = new Gson().fromJson(dataContent, CharacterStory.class);
                        characterStory.setCharacterSkinController(new CharacterSkinController(characterStory));
                        File skin = new File(characterFolder, "main.png");
                        characterStory.getCharacterSkinController().getSkins().add(skin);
                        characterStory.getCharacterSkinController().setCurrentSkin(skin);
                        characterStory.getCharacterSkinController().cacheSkins();
                        characterStory.setScene(scene);
                        scene.addNpc(characterStory);
                    } catch (IOException e) {
                        NarrativeCraftMod.LOG.warn("Npc folder does not exists, passing...");
                    }
                }
            }
        }

        // Animations
        File animationsFolder = new File(dataFolder, "animations");
        Gson gson = new GsonBuilder().registerTypeAdapter(Action.class, new ActionDeserializer()).create();
        if(animationsFolder.exists()) {
            File[] animationsFile = animationsFolder.listFiles();
            if(animationsFile != null) {
                for(File animationFile : animationsFile) {
                    try {
                        String content = Files.readString(animationFile.toPath());
                        Animation animation = gson.fromJson(content, Animation.class);
                        CharacterStory characterStory = null;
                        if(animation.getCharacter() != null) {
                            if(animation.getCharacter().getCharacterType() == CharacterStory.CharacterType.MAIN) {
                                characterStory = NarrativeCraftMod.getInstance().getCharacterManager().getCharacter(animation.getCharacter().getName());
                                characterStory.getCharacterSkinController().setCurrentSkin(characterStory.getCharacterSkinController().getSkinFile(animation.getSkinName()));
                            } else if(animation.getCharacter().getCharacterType() == CharacterStory.CharacterType.NPC) {
                                characterStory = scene.getNpc(animation.getCharacter().getName());
                            }
                            if(characterStory != null) {
                                animation.setCharacter(characterStory);
                            }
                        }
                        animation.setScene(scene);
                        scene.addAnimation(animation);
                    } catch (IOException e) {
                        NarrativeCraftMod.LOG.warn("Animation file {} does not exists, passing...", animationFile.getName());
                    }
                }
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

        // Cutscenes
        File cutsceneFile = new File(dataFolder, "cutscenes" + NarrativeCraftFile.EXTENSION_DATA_FILE);
        if(cutsceneFile.exists()) {
            try {
                String content = Files.readString(cutsceneFile.toPath());
                Type listType = new TypeToken<List<Cutscene>>() {}.getType();
                List<Cutscene> cutscenes = new Gson().fromJson(content, listType);
                if(cutscenes != null) {
                    for (Cutscene cutscene : cutscenes) {
                        cutscene.setAnimationList(new ArrayList<>());
                        if(cutscene.getAnimationListString() == null) {
                            cutscene.setAnimationListString(new ArrayList<>());
                        }
                        cutscene.setScene(scene);
                        for (String animationName : cutscene.getAnimationListString()) {
                            Animation animation = scene.getAnimationByName(animationName);
                            animation.setScene(scene);
                            cutscene.getAnimationList().add(animation);
                        }
                        for(Subscene subscene : cutscene.getSubsceneList()) {
                            subscene.setAnimationList(new ArrayList<>());
                            for (String animationName : subscene.getAnimationNameList()) {
                                Animation animation = scene.getAnimationByName(animationName);
                                animation.setScene(scene);
                                subscene.getAnimationList().add(animation);
                            }
                            subscene.setScene(scene);
                        }
                    }
                    scene.setCutsceneList(cutscenes);
                }
            } catch (IOException e) {
                NarrativeCraftMod.LOG.warn("Cutscene file does not exists, passing...");
            }
        }

        // Camera Angles
        File cameraAnglesFile = new File(dataFolder, "camera_angles" + NarrativeCraftFile.EXTENSION_DATA_FILE);
        if(cameraAnglesFile.exists()) {
            try {
                Gson gson1 = new GsonBuilder().registerTypeAdapter(Action.class, new ActionDeserializer()).create();
                String content = Files.readString(cameraAnglesFile.toPath());
                Type listType = new TypeToken<List<CameraAngleGroup>>() {}.getType();
                List<CameraAngleGroup> cameraAngleGroupList = gson1.fromJson(content, listType);
                if(cameraAngleGroupList != null) {
                    for (CameraAngleGroup cameraAngleGroup : cameraAngleGroupList) {
                        cameraAngleGroup.setScene(scene);
                        for(CharacterStoryData characterStoryData : cameraAngleGroup.getCharacterStoryDataList()) {
                            CharacterStory characterStory = null;
                            if(characterStoryData.getCharacterStory().getCharacterType() == CharacterStory.CharacterType.MAIN) {
                                characterStory = NarrativeCraftMod.getInstance().getCharacterManager().getCharacter(characterStoryData.getCharacterStory().getName());
                                characterStory.getCharacterSkinController().setCurrentSkin(characterStory.getCharacterSkinController().getSkinFile(characterStoryData.getSkinName()));
                            } else if(characterStoryData.getCharacterStory().getCharacterType() == CharacterStory.CharacterType.NPC) {
                                characterStory = scene.getNpc(characterStoryData.getCharacterStory().getName());
                            }
                            if(characterStory != null) {
                                characterStoryData.setCharacterStory(characterStory);
                            }
                        }
                    }
                    scene.setCameraAngleGroupList(cameraAngleGroupList);
                }
            } catch (IOException e) {
                NarrativeCraftMod.LOG.warn("Camera angles file does not exists, passing...");
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
            NarrativeCraftFile.updateMainInkFile();
            return true;
        } else {
            return false;
        }
    }

    public SuggestionProvider<CommandSourceStack> getChapterSuggestions() {
        return (context, builder) -> {
            if(chapters == null) return builder.buildFuture();
            for (Chapter chapter : chapters) {
                builder.suggest(chapter.getIndex());
            }
            return builder.buildFuture();
        };
    }

    public boolean chapterExists(int chapterIndex) {
        for(Chapter chapter : chapters) {
            if(chapter.getIndex() == chapterIndex){
                return true;
            }
        }
        return false;
    }

    public Chapter getChapterByIndex(int chapterIndex) {
        for(Chapter chapter : chapters) {
            if(chapter.getIndex() == chapterIndex){
                return chapter;
            }
        }
        return null;
    }

    public void removeChapter(Chapter chapter) {
        chapters.remove(chapter);
    }

    public SuggestionProvider<CommandSourceStack> getSceneSuggestionsByChapter() {
        return (context, builder) -> {
            int chapterIndex = IntegerArgumentType.getInteger(context, "chapter_index");
            Chapter chapter = getChapterByIndex(chapterIndex);
            if(chapter == null) return builder.buildFuture();
            for (Scene scene : chapter.getSceneList()) {
                if(scene.getName().split(" ").length > 1) {
                    builder.suggest("\"" + scene.getName() + "\"");
                } else {
                    builder.suggest(scene.getName());
                }
            }
            return builder.buildFuture();
        };
    }

    public SuggestionProvider<CommandSourceStack> getSubscenesOfScenesSuggestions() {
        return (context, builder) -> {
            PlayerSession playerSession = Utils.getSessionOrNull(context.getSource().getPlayer());
            if(playerSession == null) return builder.buildFuture();
            for (Subscene subscene : playerSession.getScene().getSubsceneList()) {
                if(subscene.getName().split(" ").length > 1) {
                    builder.suggest("\"" + subscene.getName() + "\"");
                } else {
                    builder.suggest(subscene.getName());
                }
            }
            return builder.buildFuture();
        };
    }
}

