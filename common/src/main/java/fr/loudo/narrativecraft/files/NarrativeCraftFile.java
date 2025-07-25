package fr.loudo.narrativecraft.files;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.NarrativeUserOptions;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleGroup;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.Cutscene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import fr.loudo.narrativecraft.narrative.dialog.DialogData;
import fr.loudo.narrativecraft.narrative.recordings.actions.Action;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionGsonParser;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.StorySave;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NarrativeCraftFile {

    public static final String EXTENSION_SCRIPT_FILE = ".ink";
    public static final String EXTENSION_DATA_FILE = ".json";

    private static final String DIRECTORY_NAME = NarrativeCraftMod.MOD_ID;

    private static final String BUILD_DIRECTORY_NAME = "build";
    private static final String CHAPTERS_DIRECTORY_NAME = "chapters";
    private static final String SCENES_DIRECTORY_NAME = "scenes";
    private static final String CHARACTERS_DIRECTORY_NAME = "characters";
    private static final String SAVES_DIRECTORY_NAME = "saves";
    private static final String MAIN_INK_NAME = "main" + EXTENSION_SCRIPT_FILE;

    public static final String CAMERA_ANGLES_FILE_NAME = "camera_angles" + EXTENSION_DATA_FILE;
    public static final String CUTSCENES_FILE_NAME = "cutscenes" + EXTENSION_DATA_FILE;
    public static final String DETAILS_FILE_NAME = "details" + EXTENSION_DATA_FILE;
    public static final String SUBSCENES_FILE_NAME = "subscenes" + EXTENSION_DATA_FILE;
    public static final String DIALOG_FILE_NAME = "dialog" + EXTENSION_DATA_FILE;
    public static final String SAVE_FILE_NAME = "save" + EXTENSION_DATA_FILE;
    public static final String DATA_FILE_NAME = "data" + EXTENSION_DATA_FILE;
    public static final String STORY_FILE_NAME = "story" + EXTENSION_DATA_FILE;
    public static final String MAIN_SCREEN_BACKGROUND_FILE_NAME = "main_screen_background" + EXTENSION_DATA_FILE;
    public static final String USER_OPTIONS_FILE_NAME = "user_options" + EXTENSION_DATA_FILE;

    public static final String ANIMATIONS_FOLDER_NAME = "animations";
    public static final String NPC_FOLDER_NAME = "npc";
    public static final String DATA_FOLDER_NAME = "data";
    public static final String SKINS_FOLDER_NAME = "skins";

    public static File mainDirectory;
    public static File chaptersDirectory;
    public static File characterDirectory;
    public static File savesDirectory;
    public static File buildDirectory;
    public static File dataDirectory;
    public static File mainInkFile;

    public static void init(MinecraftServer server) {
        File mainDirectoryCheck = new File(server.getWorldPath(LevelResource.ROOT).toFile(), DIRECTORY_NAME);
        NarrativeCraftMod.firstTime = !mainDirectoryCheck.exists();
        mainDirectory = createDirectory(server.getWorldPath(LevelResource.ROOT).toFile(), DIRECTORY_NAME);
        chaptersDirectory = createDirectory(mainDirectory, CHAPTERS_DIRECTORY_NAME);
        characterDirectory = createDirectory(mainDirectory, CHARACTERS_DIRECTORY_NAME);
        savesDirectory = createDirectory(mainDirectory, SAVES_DIRECTORY_NAME);
        buildDirectory = createDirectory(mainDirectory, BUILD_DIRECTORY_NAME);
        dataDirectory = createDirectory(mainDirectory, DATA_FOLDER_NAME);
        mainInkFile = createFile(mainDirectory, MAIN_INK_NAME);
        createGlobalDialogValues();
    }

    public static File getDetailsFile(File file) {
        return createFile(file, DETAILS_FILE_NAME);
    }

    public static DialogData getGlobalDialogValues() {
        createGlobalDialogValues();
        File dialogFile = new File(dataDirectory, DIALOG_FILE_NAME);
        try {
            String dialogContent = Files.readString(dialogFile.toPath());
            return new Gson().fromJson(dialogContent, DialogData.class);
        } catch (IOException ignored) {
        }
        return null;
    }

    public static void createGlobalDialogValues() {
        if (!dataDirectory.exists()) dataDirectory = createDirectory(mainDirectory, DATA_FOLDER_NAME);
        File dialogFile = createFile(dataDirectory, DIALOG_FILE_NAME);
        try (Writer writer = new BufferedWriter(new FileWriter(dialogFile))) {
            new Gson().toJson(DialogData.defaultValues(), writer);
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't create global dialog values! {}", e.getMessage());
        }
    }

    public static void updateUserOptions() {
        File dialogUserValue = createFile(dataDirectory, USER_OPTIONS_FILE_NAME);
        try {
            try (Writer writer = new BufferedWriter(new FileWriter(dialogUserValue))) {
                new Gson().toJson(NarrativeCraftMod.getInstance().getNarrativeUserOptions(), writer);
            } catch (IOException e) {
                NarrativeCraftMod.LOG.error("Couldn't update dialog user values! ", e);
            }
        } catch (JsonIOException e) {
            NarrativeCraftMod.LOG.error("Couldn't update dialog user values! ", e);
        }
    }

    public static NarrativeUserOptions loadUserOptions() {
        File dialogUserValue = createFile(dataDirectory, USER_OPTIONS_FILE_NAME);
        try {
            String content = Files.readString(dialogUserValue.toPath());
            if (content.isEmpty()) return null;
            return new Gson().fromJson(content, NarrativeUserOptions.class);
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't read dialog user values! ", e);
        }
        return null;
    }

    public static void updateGlobalDialogValues(DialogData dialogData) {
        try {
            createGlobalDialogValues();
            File dialogFile = createFile(dataDirectory, DIALOG_FILE_NAME);
            try (Writer writer = new BufferedWriter(new FileWriter(dialogFile))) {
                new Gson().toJson(dialogData, writer);
            } catch (IOException e) {
                NarrativeCraftMod.LOG.error("Couldn't global dialog values! ", e);
            }
        } catch (Exception ignored) {
        }
    }

    public static boolean createChapterDirectory(Chapter chapter) {
        File chapterFolder = createDirectory(chaptersDirectory, String.valueOf(chapter.getIndex()));
        try {
            File chapterInkFile = createFile(chapterFolder, "chapter_" + chapter.getIndex() + EXTENSION_SCRIPT_FILE);
            File detailsFile = getDetailsFile(chapterFolder);

            String content = String.format("{\"name\":\"%s\",\"description\":\"%s\"}", chapter.getName(), chapter.getDescription());
            try (Writer writer = new BufferedWriter(new FileWriter(detailsFile))) {
                writer.write(content);
            }
            try (Writer writer = new BufferedWriter(new FileWriter(chapterInkFile))) {
                writer.write("=== chapter_" + chapter.getIndex() + " ===");
            }
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't create chapter!  " + e.getMessage());
            return false;
        }
    }

    public static boolean updateChapterDetails(Chapter chapter, String name, String description) {
        File chapterFolder = new File(chaptersDirectory, String.valueOf(chapter.getIndex()));
        File chapterDetails = getDetailsFile(chapterFolder);
        try {
            String content = String.format("{\"name\":\"%s\",\"description\":\"%s\"}", name, description);
            try (Writer writer = new BufferedWriter(new FileWriter(chapterDetails))) {
                writer.write(content);
            }
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't update chapter {} details file! {}", chapter.getIndex(), e);
            return false;
        }
    }

    public static boolean createSceneFolder(Scene scene) {
        File chapterFolder = createDirectory(chaptersDirectory, String.valueOf(scene.getChapter().getIndex()));
        if (!chapterFolder.exists()) {
            if (!createChapterDirectory(scene.getChapter())) return false;
            chapterFolder = new File(chaptersDirectory, String.valueOf(scene.getChapter().getIndex()));
        }
        File scenesFolder = createDirectory(chapterFolder.getAbsoluteFile(), SCENES_DIRECTORY_NAME);
        File sceneFolder = createDirectory(scenesFolder, getSnakeCaseName(scene.getName()));

        File dataFolder = createDirectory(sceneFolder, DATA_FOLDER_NAME);

        try {
            File detailsFile = getDetailsFile(dataFolder);
            File sceneScriptFile = createFile(sceneFolder, getSnakeCaseName(scene.getName()) + EXTENSION_SCRIPT_FILE);

            createDirectory(dataFolder, NPC_FOLDER_NAME);
            createDirectory(dataFolder, ANIMATIONS_FOLDER_NAME);
            createFile(dataFolder, CUTSCENES_FILE_NAME);
            createFile(dataFolder, SUBSCENES_FILE_NAME);
            createFile(dataFolder, CAMERA_ANGLES_FILE_NAME);

            String content = String.format("{\"name\":\"%s\",\"description\":\"%s\",\"placement\":%s}", scene.getName(), scene.getDescription(), scene.getPlacement());
            try (Writer writer = new BufferedWriter(new FileWriter(detailsFile))) {
                writer.write(content);
            }
            try (Writer writer = new BufferedWriter(new FileWriter(sceneScriptFile))) {
                writer.write(getKnotSceneName(scene) + "\n# on enter");
            }
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't create scene! {}", e.getMessage());
            return false;
        }
    }

    public static boolean updateSceneDetails(Scene scene, String name, String description, int placement) {
        File chapterFolder = new File(chaptersDirectory, String.valueOf(scene.getChapter().getIndex()));
        File scenesFolder = new File(chapterFolder, SCENES_DIRECTORY_NAME);
        File sceneFolder = new File(scenesFolder, getSnakeCaseName(scene.getName()));
        try {
            File newSceneFolder = new File(scenesFolder, getSnakeCaseName(name));
            if (!scene.getName().equalsIgnoreCase(name)) {
                Files.move(sceneFolder.toPath(), newSceneFolder.toPath());
                sceneFolder = newSceneFolder;
            }
            File sceneDetails = getDetailsFile(new File(sceneFolder, DATA_FOLDER_NAME));
            String content = String.format("{\"name\":\"%s\",\"description\":\"%s\",\"placement\":%s}", name, description, placement);
            try (Writer writer = new BufferedWriter(new FileWriter(sceneDetails))) {
                writer.write(content);
            }
            File scriptFile = new File(sceneFolder, getSnakeCaseName(scene.getName()) + EXTENSION_SCRIPT_FILE);
            return scriptFile.renameTo(new File(sceneFolder, getSnakeCaseName(name) + EXTENSION_SCRIPT_FILE));
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't update scene {} details file of chapter {}! {}", scene.getName(), scene.getChapter().getIndex(), e);
            return false;
        }
    }

    public static void updateKnotSceneNameFromChapter(Chapter chapter, String oldName, String newName) {
        File chapterFolder = new File(chaptersDirectory, String.valueOf(chapter.getIndex()));
        File scenesFolder = new File(chapterFolder, SCENES_DIRECTORY_NAME);
        try {
            for (Scene scene : chapter.getSceneList()) {
                File sceneFolder = new File(scenesFolder, getSnakeCaseName(scene.getName()));
                File scriptFile = new File(sceneFolder, getSnakeCaseName(scene.getName()) + EXTENSION_SCRIPT_FILE);
                String scriptContent = Files.readString(scriptFile.toPath());
                if (scriptContent.contains(NarrativeCraftFile.getChapterSceneSnakeCase(chapter.getIndex(), oldName))) {
                    scriptContent = scriptContent.replace(getChapterSceneSnakeCase(chapter.getIndex(), oldName), getChapterSceneSnakeCase(chapter.getIndex(), newName));
                    try (Writer writer = new BufferedWriter(new FileWriter(scriptFile))) {
                        writer.write(scriptContent);
                    }
                }
            }
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't update knot scene name file of chapter {} ! {}", chapter.getIndex(), e);
        }
    }

    private static void updateCharacterSceneInkFile(String oldName, String newName) {
        List<Chapter> chapters = NarrativeCraftMod.getInstance().getChapterManager().getChapters();
        for (Chapter chapter : chapters) {
            File chapterFolder = new File(chaptersDirectory, String.valueOf(chapter.getIndex()));
            for (Scene scene : chapter.getSceneList()) {
                try {
                    File scenesFolder = new File(chapterFolder, SCENES_DIRECTORY_NAME);
                    File sceneFolder = new File(scenesFolder, getSnakeCaseName(scene.getName()));
                    File scriptFile = new File(sceneFolder, getSnakeCaseName(scene.getName()) + EXTENSION_SCRIPT_FILE);
                    String scriptContent = Files.readString(scriptFile.toPath());

                    Pattern pattern = Pattern.compile("\\b" + Pattern.quote(oldName) + "\\b", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(scriptContent);
                    boolean found = matcher.find();
                    if (found) {
                        StringBuilder sb = new StringBuilder();
                        do {
                            matcher.appendReplacement(sb, newName);
                        } while (matcher.find());
                        matcher.appendTail(sb);

                        try (Writer writer = new BufferedWriter(new FileWriter(scriptFile))) {
                            writer.write(sb.toString());
                        }
                    }
                } catch (IOException e) {
                    NarrativeCraftMod.LOG.error("Couldn't update character name on scene file {} of chapter {} ! {}", chapter.getIndex(), scene.getName(), e);
                    return;
                }
            }
        }
    }

    public static boolean updateCutsceneFile(Scene scene) {
        File dataFolder = getDataFolderOfScene(scene);
        File cutsceneFile = new File(dataFolder, CUTSCENES_FILE_NAME);
        Gson gson = new GsonBuilder().create();
        for (Cutscene cutscene : scene.getCutsceneList()) {
            cutscene.getAnimationListString().clear();
            for (Animation animation : cutscene.getAnimationList()) {
                cutscene.getAnimationListString().add(animation.getName());
            }
        }
        try (Writer writer = new BufferedWriter(new FileWriter(cutsceneFile))) {
            gson.toJson(scene.getCutsceneList(), writer);
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't update cutscenes file of scene {} of chapter {} ! {}", scene.getName(), scene.getChapter().getIndex(), e);
            return false;
        }
    }

    public static boolean updateSubsceneFile(Scene scene) {
        File dataFolder = getDataFolderOfScene(scene);
        File subscenesFile = new File(dataFolder, SUBSCENES_FILE_NAME);
        Gson gson = new GsonBuilder().create();
        for (Subscene subscene : scene.getSubsceneList()) {
            subscene.getAnimationNameList().clear();
            for (Animation animation : subscene.getAnimationList()) {
                subscene.getAnimationNameList().add(animation.getName());
            }
        }
        updateCutsceneFile(scene);
        try (Writer writer = new BufferedWriter(new FileWriter(subscenesFile))) {
            gson.toJson(scene.getSubsceneList(), writer);
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't update subscenes file of scene {} of chapter {} ! {}", scene.getName(), scene.getChapter().getIndex(), e);
            return false;
        }
    }

    public static boolean updateCameraAnglesFile(Scene scene) {
        File dataFolder = getDataFolderOfScene(scene);
        File subscenesFile = new File(dataFolder, CAMERA_ANGLES_FILE_NAME);
        Gson gson = new GsonBuilder().create();
        try (Writer writer = new BufferedWriter(new FileWriter(subscenesFile))) {
            gson.toJson(scene.getCameraAngleGroupList(), writer);
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't update camera angles file of scene {} of chapter {} ! {}", scene.getName(), scene.getChapter().getIndex(), e);
            return false;
        }
    }

    public static void updateMainScreenBackgroundFile(CameraAngleGroup cameraAngleGroup) {
        File mainScreenFile = createFile(dataDirectory, MAIN_SCREEN_BACKGROUND_FILE_NAME);
        Gson gson = new GsonBuilder().create();
        try (Writer writer = new BufferedWriter(new FileWriter(mainScreenFile))) {
            gson.toJson(cameraAngleGroup, writer);
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't update main screen background file ! ", e);
        }
    }

    public static boolean updateAnimationFile(Animation animation) {
        File dataFolder = getDataFolderOfScene(animation.getScene());
        File animationFolder = createDirectory(dataFolder, ANIMATIONS_FOLDER_NAME);
        File animationFile = createFile(animationFolder, getSnakeCaseName(animation.getName()) + EXTENSION_DATA_FILE);
        Gson gson = new GsonBuilder().registerTypeAdapter(Action.class, new ActionGsonParser()).create();
        try (Writer writer = new BufferedWriter(new FileWriter(animationFile))) {
            gson.toJson(animation, writer);
            updateSubsceneFile(animation.getScene());
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't update animation {} file of scene {} of chapter {} ! {}", animation.getName(), animation.getScene().getName(), animation.getScene().getChapter().getIndex(), e);
            return false;
        }
    }

    public static boolean updateCharacterFolder(String oldName, String newName) {
        CharacterStory characterStory = NarrativeCraftMod.getInstance().getCharacterManager().getCharacter(newName);
        File characterFolderNew = new File(characterDirectory, Utils.getSnakeCase(newName));
        File characterFolderOld = new File(characterDirectory, Utils.getSnakeCase(oldName));
        File characterFile = new File(characterFolderNew, DATA_FILE_NAME);
        File saveFile = new File(savesDirectory, SAVE_FILE_NAME);
        try {
            Files.move(characterFolderOld.toPath(), characterFolderNew.toPath());
            List<Chapter> chapters = NarrativeCraftMod.getInstance().getChapterManager().getChapters();
            for (Chapter chapter : chapters) {
                for (Scene scene : chapter.getSceneList()) {
                    for (Animation animation : scene.getAnimationList()) {
                        if (animation.getCharacter().getName().equalsIgnoreCase(oldName) || animation.getCharacter().getName().equalsIgnoreCase(newName)) {
                            animation.setCharacter(characterStory);
                            updateAnimationFile(animation);
                        }
                    }
                    for (CameraAngleGroup cameraAngleGroup : scene.getCameraAngleGroupList()) {
                        for (CharacterStoryData characterStoryData : cameraAngleGroup.getCharacterStoryDataList()) {
                            if (characterStoryData.getCharacterStory().getName().equalsIgnoreCase(oldName) || characterStoryData.getCharacterStory().getName().equalsIgnoreCase(newName)) {
                                characterStoryData.setCharacterStory(characterStory);
                                updateCameraAnglesFile(scene);
                            }
                        }
                    }
                }
            }
            StorySave save = getSave();
            if (save != null) {
                for (CharacterStoryData characterStoryData : save.getCharacterStoryDataList()) {
                    if (characterStoryData.getCharacterStory().getName().equalsIgnoreCase(oldName)) {
                        characterStoryData.getCharacterStory().setName(newName);
                    }
                }
                try (Writer writer = new BufferedWriter(new FileWriter(saveFile))) {
                    new Gson().toJson(save, writer);
                }
            }
            try (Writer writer = new BufferedWriter(new FileWriter(characterFile))) {
                new Gson().toJson(characterStory, writer);
            }
            updateCharacterSceneInkFile(oldName, newName);
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't update character {} file! {}", oldName, e);
            return false;
        }
    }

    public static boolean updateCharacterFolder(CharacterStory characterStory) {
        File characterFolderNew = new File(characterDirectory, Utils.getSnakeCase(characterStory.getName()));
        File characterFile = new File(characterFolderNew, DATA_FILE_NAME);
        File saveFile = new File(savesDirectory, SAVE_FILE_NAME);
        try {
            List<Chapter> chapters = NarrativeCraftMod.getInstance().getChapterManager().getChapters();
            for (Chapter chapter : chapters) {
                for (Scene scene : chapter.getSceneList()) {
                    for (Animation animation : scene.getAnimationList()) {
                        if (animation.getCharacter().getName().equalsIgnoreCase(characterStory.getName())) {
                            animation.setCharacter(characterStory);
                            updateAnimationFile(animation);
                        }
                    }
                    for (CameraAngleGroup cameraAngleGroup : scene.getCameraAngleGroupList()) {
                        for (CharacterStoryData characterStoryData : cameraAngleGroup.getCharacterStoryDataList()) {
                            if (characterStoryData.getCharacterStory().getName().equalsIgnoreCase(characterStory.getName())) {
                                characterStoryData.setCharacterStory(characterStory);
                                updateCameraAnglesFile(scene);
                            }
                        }
                    }
                }
            }
            StorySave save = getSave();
            if (save != null) {
                for (CharacterStoryData characterStoryData : save.getCharacterStoryDataList()) {
                    if (characterStoryData.getCharacterStory().getName().equalsIgnoreCase(characterStory.getName())) {
                        characterStoryData.setCharacterStory(characterStory);
                    }
                }
                try (Writer writer = new BufferedWriter(new FileWriter(saveFile))) {
                    new Gson().toJson(save, writer);
                }
            }
            try (Writer writer = new BufferedWriter(new FileWriter(characterFile))) {
                new Gson().toJson(characterStory, writer);
            }
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't update character {} file! {}", characterStory.getName(), e);
            return false;
        }
    }

    public static boolean updateNpcSceneFolder(String oldName, String newName, Scene scene) {
        File sceneFolder = getSceneFolder(scene);
        File dataFile = createDirectory(sceneFolder, DATA_FOLDER_NAME);
        File npcFolder = createDirectory(dataFile, NPC_FOLDER_NAME);
        CharacterStory characterStory = scene.getNpc(newName);
        File characterFolderNew = createDirectory(npcFolder, Utils.getSnakeCase(newName));
        File characterFolderOld = new File(npcFolder, Utils.getSnakeCase(oldName));
        File characterFile = createFile(characterFolderNew, DATA_FILE_NAME);
        File saveFile = createFile(savesDirectory, SAVE_FILE_NAME);
        try {
            Files.move(characterFolderOld.toPath(), characterFolderNew.toPath());
            for (Animation animation : scene.getAnimationList()) {
                if (animation.getCharacter().getName().equalsIgnoreCase(oldName) || animation.getCharacter().getName().equalsIgnoreCase(newName)) {
                    animation.setCharacter(characterStory);
                    updateAnimationFile(animation);
                }
            }
            for (CameraAngleGroup cameraAngleGroup : scene.getCameraAngleGroupList()) {
                for (CharacterStoryData characterStoryData : cameraAngleGroup.getCharacterStoryDataList()) {
                    if (characterStoryData.getCharacterStory().getName().equalsIgnoreCase(oldName) || characterStoryData.getCharacterStory().getName().equalsIgnoreCase(newName)) {
                        characterStoryData.setCharacterStory(characterStory);
                        updateCameraAnglesFile(scene);
                    }
                }
            }
            StorySave save = getSave();
            if (save != null) {
                for (CharacterStoryData characterStoryData : save.getCharacterStoryDataList()) {
                    if (characterStoryData.getCharacterStory().getName().equalsIgnoreCase(oldName)) {
                        characterStoryData.getCharacterStory().setName(newName);
                    }
                }
                try (Writer writer = new BufferedWriter(new FileWriter(saveFile))) {
                    new Gson().toJson(save, writer);
                }
            }
            try (Writer writer = new BufferedWriter(new FileWriter(characterFile))) {
                new Gson().toJson(characterStory, writer);
            }
            updateCharacterSceneInkFile(oldName, newName);
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't update npc {} folder! {}", oldName, e);
            return false;
        }
    }

    public static boolean updateNpcSceneFolder(CharacterStory characterStory, Scene scene) {
        File sceneFolder = getSceneFolder(scene);
        File dataFile = createDirectory(sceneFolder, DATA_FOLDER_NAME);
        File npcFolder = createDirectory(dataFile, NPC_FOLDER_NAME);
        File characterFolder = createDirectory(npcFolder, Utils.getSnakeCase(characterStory.getName()));
        File characterFile = createFile(characterFolder, DATA_FILE_NAME);
        File saveFile = createFile(savesDirectory, SAVE_FILE_NAME);
        try {
            for (Animation animation : scene.getAnimationList()) {
                if (animation.getCharacter().getName().equalsIgnoreCase(characterStory.getName())) {
                    animation.setCharacter(characterStory);
                    updateAnimationFile(animation);
                }
            }
            for (CameraAngleGroup cameraAngleGroup : scene.getCameraAngleGroupList()) {
                for (CharacterStoryData characterStoryData : cameraAngleGroup.getCharacterStoryDataList()) {
                    if (characterStoryData.getCharacterStory().getName().equalsIgnoreCase(characterStory.getName())) {
                        characterStoryData.setCharacterStory(characterStory);
                        updateCameraAnglesFile(scene);
                    }
                }
            }
            StorySave save = getSave();
            if (save != null) {
                for (CharacterStoryData characterStoryData : save.getCharacterStoryDataList()) {
                    if (characterStoryData.getCharacterStory().getName().equalsIgnoreCase(characterStory.getName())) {
                        characterStoryData.setCharacterStory(characterStory);
                    }
                }
                try (Writer writer = new BufferedWriter(new FileWriter(saveFile))) {
                    new Gson().toJson(save, writer);
                }
            }
            try (Writer writer = new BufferedWriter(new FileWriter(characterFile))) {
                new Gson().toJson(characterStory, writer);
            }
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't update npc {} folder! {}", characterStory.getName(), e);
            return false;
        }
    }

    public static boolean createCharacterFile(CharacterStory characterStory) {
        File characterFolder = createDirectory(characterDirectory, getSnakeCaseName(characterStory.getName()));
        File characterFile = createFile(characterFolder, DATA_FILE_NAME);
        File skinsFolder = createDirectory(characterFolder, SKINS_FOLDER_NAME);
        PlayerSkin defaultPlayerSkin = DefaultPlayerSkin.get(UUID.randomUUID());
        characterStory.setModel(defaultPlayerSkin.model());
        File mainSkinFile = createFile(skinsFolder, "main.png");
        try (InputStream inputStream = Minecraft.getInstance().getResourceManager().open(defaultPlayerSkin.texture())) {
            Files.copy(inputStream, mainSkinFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
        } // Don't really need to handle

        Gson gson = new GsonBuilder().create();
        try (Writer writer = new BufferedWriter(new FileWriter(characterFile))) {
            gson.toJson(characterStory, writer);
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't create character {} file! {}", characterStory.getName(), e);
            return false;
        }
    }

    public static boolean createCharacterFileScene(CharacterStory characterStory, Scene scene) {
        File sceneFile = getSceneFolder(scene);
        File sceneDataFolder = createDirectory(sceneFile, DATA_FOLDER_NAME);
        File npcFolder = createDirectory(sceneDataFolder, NPC_FOLDER_NAME);
        File characterFolder = createDirectory(npcFolder, Utils.getSnakeCase(characterStory.getName()));
        File characterFile = createFile(characterFolder, DATA_FILE_NAME);
        PlayerSkin defaultPlayerSkin = DefaultPlayerSkin.get(UUID.randomUUID());
        characterStory.setModel(defaultPlayerSkin.model());
        File mainSkinFile = createFile(characterFolder, "main.png");
        try (InputStream inputStream = Minecraft.getInstance().getResourceManager().open(defaultPlayerSkin.texture())) {
            Files.copy(inputStream, mainSkinFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
        } // Don't really need to handle
        characterStory.getCharacterSkinController().setCurrentSkin(createFile(npcFolder, "main.png"));
        Gson gson = new GsonBuilder().create();
        try (Writer writer = new BufferedWriter(new FileWriter(characterFile))) {
            gson.toJson(characterStory, writer);
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't create npc {} folder! {}", characterStory.getName(), e);
            return false;
        }
    }

    public static void removeCharacterFolder(CharacterStory characterStory) {
        File characterFolder = createDirectory(characterDirectory, Utils.getSnakeCase(characterStory.getName()));
        deleteDirectory(characterFolder);
    }

    public static void removeNpcFolder(CharacterStory characterStory) {
        File sceneFolder = getSceneFolder(characterStory.getScene());
        File dataFolder = createDirectory(sceneFolder, DATA_FOLDER_NAME);
        File npcFolder = createDirectory(dataFolder, NPC_FOLDER_NAME);
        File characterFolder = createDirectory(npcFolder, Utils.getSnakeCase(characterStory.getName()));
        deleteDirectory(characterFolder);
    }

    public static void removeChapterFolder(Chapter chapter) {
        File chapterFolder = createDirectory(chaptersDirectory, String.valueOf(chapter.getIndex()));
        deleteDirectory(chapterFolder);
        updateMainInkFile();
    }

    public static void removeSceneFolder(Scene scene) {
        File chapterFolder = createDirectory(chaptersDirectory, String.valueOf(scene.getChapter().getIndex()));
        File scenesFolder = createDirectory(chapterFolder, SCENES_DIRECTORY_NAME);
        File sceneFolder = createDirectory(scenesFolder, getSnakeCaseName(scene.getName()));
        deleteDirectory(sceneFolder);
        updateMainInkFile();
    }

    public static void removeAnimationFileFromScene(Animation animation) {
        File dataFolder = getDataFolderOfScene(animation.getScene());
        File animationsFolder = createDirectory(dataFolder, ANIMATIONS_FOLDER_NAME);
        File animationFile = createFile(animationsFolder, getSnakeCaseName(animation.getName()) + EXTENSION_DATA_FILE);
        animationFile.delete();
    }

    public static boolean subscenesFileExist(Scene scene) {
        File dataFolder = getDataFolderOfScene(scene);
        File subscenesFile = createFile(dataFolder, SUBSCENES_FILE_NAME);
        return subscenesFile.exists();
    }

    public static boolean cutscenesFileExist(Scene scene) {
        File dataFolder = getDataFolderOfScene(scene);
        File cutscenesFile = createFile(dataFolder, CUTSCENES_FILE_NAME);
        return cutscenesFile.exists();
    }

    public static boolean animationFileExist(Scene scene, Animation animation) {
        File dataFolder = getDataFolderOfScene(scene);
        File animationsFolder = createDirectory(dataFolder, ANIMATIONS_FOLDER_NAME);
        File animationFile = createFile(animationsFolder, getSnakeCaseName(animation.getName()) + EXTENSION_DATA_FILE);
        return animationFile.exists();
    }

    public static File animationFolder(Scene scene) {
        File dataFolder = getDataFolderOfScene(scene);
        return createDirectory(dataFolder, ANIMATIONS_FOLDER_NAME);
    }

    public static String getStoryContent() throws IOException {
        return Files.readString(getStoryFile().toPath());
    }

    public static File getStoryFile() {
        File buildFolder = createDirectory(mainDirectory, BUILD_DIRECTORY_NAME);
        return new File(buildFolder, STORY_FILE_NAME);
    }

    public static List<String> readSceneLines(Scene scene) {
        File sceneScript = getSceneInkFile(scene);
        try {
            return Arrays.stream(Files.readString(sceneScript.toPath()).split("\n")).toList();
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public static File getSceneInkFile(Scene scene) {
        File chapterFolder = createDirectory(chaptersDirectory, String.valueOf(scene.getChapter().getIndex()));
        File scenesFolder = createDirectory(chapterFolder, SCENES_DIRECTORY_NAME);
        File sceneFolder = createDirectory(scenesFolder, scene.getSnakeCase());
        return createFile(sceneFolder, scene.getSnakeCase() + EXTENSION_SCRIPT_FILE);
    }

    public static File getSceneFolder(Scene scene) {
        File chapterFolder = createDirectory(chaptersDirectory, String.valueOf(scene.getChapter().getIndex()));
        File scenesFolder = createDirectory(chapterFolder, SCENES_DIRECTORY_NAME);
        return createDirectory(scenesFolder, scene.getSnakeCase());
    }

    public static void updateMainInkFile() {
        List<Chapter> chapterList = NarrativeCraftMod.getInstance().getChapterManager().getChapters();
        StringBuilder stringBuilder = new StringBuilder();
        String chapterPath = CHAPTERS_DIRECTORY_NAME + "\\";
        String scenesPath = SCENES_DIRECTORY_NAME + "\\";
        for (Chapter chapter : chapterList) {
            stringBuilder.append("INCLUDE ")
                    .append(chapterPath)
                    .append(chapter.getIndex())
                    .append("\\")
                    .append("chapter_")
                    .append(chapter.getIndex())
                    .append(EXTENSION_SCRIPT_FILE)
                    .append("\n");
            for (Scene scene : chapter.getSceneList()) {
                stringBuilder.append("INCLUDE ")
                        .append(chapterPath)
                        .append(chapter.getIndex())
                        .append("\\")
                        .append(scenesPath)
                        .append(getSnakeCaseName(scene.getName()))
                        .append("\\")
                        .append(getSnakeCaseName(scene.getName()))
                        .append(EXTENSION_SCRIPT_FILE)
                        .append("\n");
            }
        }
        stringBuilder.append("\n").append("-> chapter_1");
        try (Writer writer = new BufferedWriter(new FileWriter(mainInkFile))) {
            writer.write(stringBuilder.toString());
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Can't update main ink file! {}", e.getMessage());
        }
    }

    public static boolean writeSave(StoryHandler storyHandler, boolean newScene) {
        try {
            File saveFile = createFile(savesDirectory, SAVE_FILE_NAME);
            StorySave save = new StorySave(storyHandler, newScene);
            try (Writer writer = new BufferedWriter(new FileWriter(saveFile))) {
                new Gson().toJson(save, writer);
            }
            return true;
        } catch (Exception e) {
            storyHandler.crash(e, false);
            NarrativeCraftMod.LOG.error("Can't write on save file!", e);
            return false;
        }
    }

    public static StorySave getSave() {
        File saveFile = new File(savesDirectory, SAVE_FILE_NAME);
        try {
            String saveContent = Files.readString(saveFile.toPath());
            Gson gson = new GsonBuilder().create();
            StorySave save = gson.fromJson(saveContent, StorySave.class);
            if(save == null) return null;
            Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getChapterByIndex(save.getChapterIndex());
            Scene scene = chapter.getSceneByName(save.getSceneName());
            for (CharacterStoryData characterStoryData : save.getCharacterStoryDataList()) {
                CharacterStory characterStory = null;
                if (characterStoryData.getCharacterStory().getCharacterType() == CharacterStory.CharacterType.MAIN) {
                    characterStory = NarrativeCraftMod.getInstance().getCharacterManager().getCharacter(characterStoryData.getCharacterStory().getName());
                    characterStoryData.setCharacterStory(characterStory);
                } else if (characterStoryData.getCharacterStory().getCharacterType() == CharacterStory.CharacterType.NPC) {
                    characterStory = scene.getNpc(characterStoryData.getCharacterStory().getName());
                }
                characterStoryData.setCharacterStory(characterStory);
            }
            return save;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Can't read save file!", e);
            return null;
        }
    }

    public static void removeSave() {
        File saveFile = createFile(savesDirectory, SAVE_FILE_NAME);
        saveFile.delete();
    }

    public static String getChapterSceneSnakeCase(Scene scene) {
        return "chapter_" + scene.getChapter().getIndex() + "_" + getSnakeCaseName(scene.getName());
    }

    private static String getChapterSceneSnakeCase(int chapterIndex, String sceneName) {
        return "chapter_" + chapterIndex + "_" + getSnakeCaseName(sceneName);
    }

    private static String getKnotSceneName(Scene scene) {
        return "=== " + getChapterSceneSnakeCase(scene) + " ===";
    }

    private static String getSnakeCaseName(String name) {
        return String.join("_", name.toLowerCase().split(" "));
    }

    private static File getDataFolderOfScene(Scene scene) {
        File chapterFolder = createDirectory(chaptersDirectory, String.valueOf(scene.getChapter().getIndex()));
        File scenesFolder = createDirectory(chapterFolder, SCENES_DIRECTORY_NAME);
        File sceneFolder = createDirectory(scenesFolder, getSnakeCaseName(scene.getName()));
        return createDirectory(sceneFolder, DATA_FOLDER_NAME);
    }

    private static File createDirectory(File parent, String name) {
        File directory = new File(parent, name);
        if (!directory.exists()) {
            if (!directory.mkdir()) NarrativeCraftMod.LOG.error("Couldn't create directory {}!", name);
        }
        return directory;
    }

    private static File createFile(File parent, String name) {
        File file = new File(parent, name);
        if (!file.exists()) {
            try {
                if (!file.createNewFile())
                    NarrativeCraftMod.LOG.error("Couldn't create file {}!", file.getAbsolutePath());
            } catch (IOException e) {
                NarrativeCraftMod.LOG.error("Couldn't create file {}! Cause: {}", file.getAbsolutePath(), e);
            }
        }
        return file;
    }

    private static void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }

    public static File getSkinFile(CharacterStory characterStory, String name) {
        File characterFolder = createDirectory(characterDirectory, getSnakeCaseName(characterStory.getName()));
        File skinsFolder = createDirectory(characterFolder, SKINS_FOLDER_NAME);
        File skin = createFile(skinsFolder, name);
        return skin.exists() ? skin : null;
    }

    public static Cutscene getCutsceneData(Cutscene cutscene) {
        File sceneData = getDataFolderOfScene(cutscene.getScene());
        File cutsceneFile = createFile(sceneData, CUTSCENES_FILE_NAME);
        Type listType = new TypeToken<List<Cutscene>>() {
        }.getType();
        try {
            String content = Files.readString(cutsceneFile.toPath());
            List<Cutscene> cutscenes = new Gson().fromJson(content, listType);
            for (Cutscene cutscene1 : cutscenes) {
                if (cutscene.getName().equals(cutscene1.getName())) {
                    return cutscene1;
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    public static CameraAngleGroup getCameraAngleData(CameraAngleGroup cameraAngleGroup) {
        File sceneData = getDataFolderOfScene(cameraAngleGroup.getScene());
        File cameraAngleFile = createFile(sceneData, CAMERA_ANGLES_FILE_NAME);
        Type listType = new TypeToken<List<CameraAngleGroup>>() {
        }.getType();
        try {
            String content = Files.readString(cameraAngleFile.toPath());
            List<CameraAngleGroup> cameraAngleGroupList = new Gson().fromJson(content, listType);
            for (CameraAngleGroup cameraAngleGroup1 : cameraAngleGroupList) {
                if (cameraAngleGroup1.getName().equals(cameraAngleGroup.getName())) {
                    return cameraAngleGroup1;
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    public static CameraAngleGroup getMainScreenBackgroundFile() {
        File mainScreenBackgroundFile = createFile(dataDirectory, MAIN_SCREEN_BACKGROUND_FILE_NAME);
        try {
            String content = Files.readString(mainScreenBackgroundFile.toPath());
            return new Gson().fromJson(content, CameraAngleGroup.class);
        } catch (IOException e) {
            return null;
        }
    }
}