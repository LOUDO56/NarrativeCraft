package fr.loudo.narrativecraft.files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleGroup;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.Cutscene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import fr.loudo.narrativecraft.narrative.dialog.DialogData;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.StorySave;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec2;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
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
    private static final String DATA_DIRECTORY_NAME = "data";
    private static final String MAIN_INK_NAME = "main" + EXTENSION_SCRIPT_FILE;

    public static File mainDirectory;
    public static File chaptersDirectory;
    public static File characterDirectory;
    public static File savesDirectory;
    public static File buildDirectory;
    public static File dataDirectory;
    public static File mainInkFile;

    public static void init(MinecraftServer server) {
        mainDirectory = createDirectory(server.getWorldPath(LevelResource.ROOT).toFile(), DIRECTORY_NAME);
        chaptersDirectory = createDirectory(mainDirectory, CHAPTERS_DIRECTORY_NAME);
        characterDirectory = createDirectory(mainDirectory, CHARACTERS_DIRECTORY_NAME);
        savesDirectory = createDirectory(mainDirectory, SAVES_DIRECTORY_NAME);
        buildDirectory = createDirectory(mainDirectory, BUILD_DIRECTORY_NAME);
        dataDirectory = createDirectory(mainDirectory, DATA_DIRECTORY_NAME);
        mainInkFile = createFile(mainDirectory, MAIN_INK_NAME);
        createGlobalDialogValues();
    }

    public static File getDetailsFile(File file) {
        return new File(file.getAbsoluteFile(), "details" + NarrativeCraftFile.EXTENSION_DATA_FILE);
    }

    public static DialogData getGlobalDialogValues() {
        createGlobalDialogValues();
        File dialogFile = new File(dataDirectory, "dialog" + EXTENSION_DATA_FILE);
        try {
            String dialogContent = Files.readString(dialogFile.toPath());
            return new Gson().fromJson(dialogContent, DialogData.class);
        } catch (IOException ignored) {}
        return null;
    }

    public static void createGlobalDialogValues() {
        if(!dataDirectory.exists()) createDirectory(mainDirectory, DATA_DIRECTORY_NAME);
        File dialogFile = new File(dataDirectory, "dialog" + EXTENSION_DATA_FILE);
        if(!dialogFile.exists()) {
            createFile(dataDirectory, "dialog" + EXTENSION_DATA_FILE);
            DialogData dialogData = new DialogData(
                    null,
                    null,
                    new Vec2(0, 0.8f),
                    -1,
                    0,
                    3,
                    4,
                    0.8f,
                    0.1f,
                    10,
                    90,
                    false,
                    0,
                    100,
                    250
            );
            try(Writer writer = new BufferedWriter(new FileWriter(dialogFile))) {
                new Gson().toJson(dialogData, writer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void updateGlobalDialogValues(DialogData dialogData) {
        try {
            createGlobalDialogValues();
            File dialogFile = new File(dataDirectory, "dialog" + EXTENSION_DATA_FILE);
            try(Writer writer = new BufferedWriter(new FileWriter(dialogFile))) {
                new Gson().toJson(dialogData, writer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (Exception ignored) {}
    }

    public static boolean createChapterDirectory(Chapter chapter) {
        File chapterFolder = createDirectory(chaptersDirectory, String.valueOf(chapter.getIndex()));
        if(!chapterFolder.exists()) return false;
        try {
            File chapterDirectory = new File(chapterFolder, SCENES_DIRECTORY_NAME);
            if(chapterDirectory.exists()) return true;
            chapterDirectory.mkdir();
            File chapterInkFile = new File(chapterFolder, "chapter_" + chapter.getIndex() + EXTENSION_SCRIPT_FILE);
            File detailsFile = getDetailsFile(chapterFolder);
            detailsFile.createNewFile();
            chapterInkFile.createNewFile();
            String content = String.format("{\"name\":\"%s\",\"description\":\"%s\"}", chapter.getName(), chapter.getDescription());
            try(Writer writer = new BufferedWriter(new FileWriter(detailsFile))) {
                writer.write(content);
            }
            try(Writer writer = new BufferedWriter(new FileWriter(chapterInkFile))) {
                writer.write("=== chapter_" + chapter.getIndex() + " ===");
            }
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't create chapter!  " + e.getStackTrace());
            return false;
        }

    }

    public static boolean updateChapterDetails(Chapter chapter, String name, String description) {
        File chapterFolder = new File(chaptersDirectory, String.valueOf(chapter.getIndex()));
        File chapterDetails = getDetailsFile(chapterFolder);
        try {
            String content = String.format("{\"name\":\"%s\",\"description\":\"%s\"}", name, description);
            try(Writer writer = new BufferedWriter(new FileWriter(chapterDetails))) {
                writer.write(content);
            }
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't update chapter {} details file! {}", chapter.getIndex(), e.getStackTrace());
            return false;
        }
    }

    public static boolean createSceneFolder(Scene scene) {
        File chapterFolder = createDirectory(chaptersDirectory, String.valueOf(scene.getChapter().getIndex()));
        if(!chapterFolder.exists()) createChapterDirectory(scene.getChapter());
        File scenesFolder = new File(chapterFolder.getAbsoluteFile(), SCENES_DIRECTORY_NAME);
        File sceneFolder = new File(scenesFolder.getAbsoluteFile(), getSnakeCaseName(scene.getName()));
        if(sceneFolder.exists()) return true;
        sceneFolder.mkdir();
        File dataFolder = new File(sceneFolder.getAbsoluteFile(), "data");
        dataFolder.mkdir();
        try {
            File detailsFile = getDetailsFile(dataFolder);
            detailsFile.createNewFile();
            File sceneScriptFile = new File(sceneFolder.getAbsoluteFile(), getSnakeCaseName(scene.getName()) + EXTENSION_SCRIPT_FILE);
            sceneScriptFile.createNewFile();
            createDirectory(dataFolder, "npc");
            new File(dataFolder.getAbsoluteFile(), "animations").mkdir();
            new File(dataFolder.getAbsoluteFile(), "cutscenes" + EXTENSION_DATA_FILE).createNewFile();
            new File(dataFolder.getAbsoluteFile(), "subscenes" + EXTENSION_DATA_FILE).createNewFile();
            new File(dataFolder.getAbsoluteFile(), "camera_angles" + EXTENSION_DATA_FILE).createNewFile();
            String content = String.format("{\"name\":\"%s\",\"description\":\"%s\"}", scene.getName(), scene.getDescription());
            try(Writer writer = new BufferedWriter(new FileWriter(detailsFile))) {
                writer.write(content);
            }
            try(Writer writer = new BufferedWriter(new FileWriter(sceneScriptFile))) {
                writer.write(getKnotSceneName(scene) + "\n#on enter");
            }
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't create scene! {}", e.getStackTrace());
            return false;
        }

    }

    public static boolean updateSceneDetails(Scene scene, String name, String description) {
        File chapterFolder = new File(chaptersDirectory, String.valueOf(scene.getChapter().getIndex()));
        File scenesFolder = new File(chapterFolder, SCENES_DIRECTORY_NAME);
        File sceneFolder = new File(scenesFolder, getSnakeCaseName(scene.getName()));
        try {
            File newSceneFolder = new File(scenesFolder, getSnakeCaseName(name));
            if(!scene.getName().equalsIgnoreCase(name)) {
                Files.move(sceneFolder.toPath(), newSceneFolder.toPath());
                sceneFolder = newSceneFolder;
            }
            File sceneDetails = getDetailsFile(new File(sceneFolder, "data"));
            String content = String.format("{\"name\":\"%s\",\"description\":\"%s\"}", name, description);
            try(Writer writer = new BufferedWriter(new FileWriter(sceneDetails))) {
                writer.write(content);
            }
            File scriptFile = new File(sceneFolder, getSnakeCaseName(scene.getName()) + EXTENSION_SCRIPT_FILE);
            scriptFile.renameTo(new File(sceneFolder, getSnakeCaseName(name) + EXTENSION_SCRIPT_FILE));
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't update scene {} details file of chapter {}! {}", scene.getName(), scene.getChapter().getIndex(), e.getStackTrace());
            return false;
        }
    }

    public static boolean updateKnotSceneNameFromChapter(Chapter chapter, String oldName, String newName) {
        File chapterFolder = new File(chaptersDirectory, String.valueOf(chapter.getIndex()));
        File scenesFolder = new File(chapterFolder, SCENES_DIRECTORY_NAME);
        try {
            for(Scene scene : chapter.getSceneList()) {
                File sceneFolder = new File(scenesFolder, getSnakeCaseName(scene.getName()));
                File scriptFile = new File(sceneFolder, getSnakeCaseName(scene.getName()) + EXTENSION_SCRIPT_FILE);
                String scriptContent = Files.readString(scriptFile.toPath());
                if(scriptContent.contains(NarrativeCraftFile.getChapterSceneSneakCase(chapter.getIndex(), oldName))) {
                    scriptContent = scriptContent.replace(getChapterSceneSneakCase(chapter.getIndex(), oldName), getChapterSceneSneakCase(chapter.getIndex(), newName));
                    try(Writer writer = new BufferedWriter(new FileWriter(scriptFile))) {
                        writer.write(scriptContent);
                    }
                }
            }
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't update knot scene name file of chapter {} ! {}", chapter.getIndex(), e.getStackTrace());
            return false;
        }
    }

    private static boolean updateCharacterSceneInkFile(String oldName, String newName) {
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
                        NarrativeCraftMod.LOG.error("Couldn't update character name on scene file {} of chapter {} ! {}", chapter.getIndex(), scene.getName(), e.getStackTrace());
                        return false;
                    }
                }
            }
            return true;
    }

    public static boolean updateCutsceneFile(Scene scene) {
        File dataFolder = getDataFolderOfScene(scene);
        File cutsceneFile = new File(dataFolder, "cutscenes" + EXTENSION_DATA_FILE);
        Gson gson = new GsonBuilder().create();
        for(Cutscene cutscene : scene.getCutsceneList()) {
            cutscene.getAnimationListString().clear();
            for (Animation animation : cutscene.getAnimationList()) {
                cutscene.getAnimationListString().add(animation.getName());
            }
        }
        try(Writer writer = new BufferedWriter(new FileWriter(cutsceneFile))) {
            gson.toJson(scene.getCutsceneList(), writer);
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't update cutscenes file of scene {} of chapter {} ! {}", scene.getName(), scene.getChapter().getIndex(), e.getStackTrace());
            return false;
        }
    }

    public static boolean updateSubsceneFile(Scene scene) {
        File dataFolder = getDataFolderOfScene(scene);
        File subscenesFile = new File(dataFolder, "subscenes" + EXTENSION_DATA_FILE);
        Gson gson = new GsonBuilder().create();
        for(Subscene subscene : scene.getSubsceneList()) {
            subscene.getAnimationNameList().clear();
            for (Animation animation : subscene.getAnimationList()) {
                subscene.getAnimationNameList().add(animation.getName());
            }
        }
        updateCutsceneFile(scene);
        try(Writer writer = new BufferedWriter(new FileWriter(subscenesFile))) {
            gson.toJson(scene.getSubsceneList(), writer);
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't update subscenes file of scene {} of chapter {} ! {}", scene.getName(), scene.getChapter().getIndex(), e.getStackTrace());
            return false;
        }
    }

    public static boolean updateCameraAnglesFile(Scene scene) {
        File dataFolder = getDataFolderOfScene(scene);
        File subscenesFile = new File(dataFolder, "camera_angles" + EXTENSION_DATA_FILE);
        Gson gson = new GsonBuilder().create();
        try(Writer writer = new BufferedWriter(new FileWriter(subscenesFile))) {
            gson.toJson(scene.getCameraAngleGroupList(), writer);
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't update camera angles file of scene {} of chapter {} ! {}", scene.getName(), scene.getChapter().getIndex(), e.getStackTrace());
            return false;
        }
    }

    public static boolean updateAnimationFile(Animation animation) {
        File dataFolder = getDataFolderOfScene(animation.getScene());
        File animationFolder = new File(dataFolder, "animations");
        if(!animationFolder.exists()) animationFolder.mkdir();
        File animationFile = createFile(animationFolder, getSnakeCaseName(animation.getName()) + EXTENSION_DATA_FILE);
        Gson gson = new GsonBuilder().create();
        try(Writer writer = new BufferedWriter(new FileWriter(animationFile))) {
            gson.toJson(animation, writer);
            updateSubsceneFile(animation.getScene());
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't update animation {} file of scene {} of chapter {} ! {}", animation.getName(), animation.getScene().getName(), animation.getScene().getChapter().getIndex(), e.getStackTrace());
            return false;
        }
    }

    public static boolean updateCharacterFolder(String oldName, String newName) {
        CharacterStory characterStory = NarrativeCraftMod.getInstance().getCharacterManager().getCharacter(newName);
        File characterFolderNew = new File(characterDirectory, Utils.getSnakeCase(newName));
        File characterFolderOld = new File(characterDirectory, Utils.getSnakeCase(oldName));
        File characterFile = new File(characterFolderNew, "data" + EXTENSION_DATA_FILE);
        File saveFile = new File(savesDirectory, "save" + EXTENSION_DATA_FILE);
        try {
            Files.move(characterFolderOld.toPath(), characterFolderNew.toPath());
            List<Chapter> chapters = NarrativeCraftMod.getInstance().getChapterManager().getChapters();
            for(Chapter chapter : chapters) {
                for(Scene scene : chapter.getSceneList()) {
                    for(Animation animation : scene.getAnimationList()) {
                        if(animation.getCharacter().getName().equalsIgnoreCase(oldName) || animation.getCharacter().getName().equalsIgnoreCase(newName)) {
                            animation.setCharacter(characterStory);
                            updateAnimationFile(animation);
                        }
                    }
                    for(CameraAngleGroup cameraAngleGroup : scene.getCameraAngleGroupList()) {
                        for(CharacterStoryData characterStoryData : cameraAngleGroup.getCharacterStoryDataList()) {
                            if(characterStoryData.getCharacterStory().getName().equalsIgnoreCase(oldName) || characterStoryData.getCharacterStory().getName().equalsIgnoreCase(newName)) {
                                characterStoryData.setCharacterStory(characterStory);
                                updateCameraAnglesFile(scene);
                            }
                        }
                    }
                }
            }
            StorySave save = getSave();
            if(save != null) {
                for(CharacterStoryData characterStoryData : save.getCharacterStoryDataList()) {
                    if(characterStoryData.getCharacterStory().getName().equalsIgnoreCase(oldName)) {
                        characterStoryData.getCharacterStory().setName(newName);
                    }
                }
                try(Writer writer = new BufferedWriter(new FileWriter(saveFile))) {
                    new Gson().toJson(save, writer);
                }
            }
            try(Writer writer = new BufferedWriter(new FileWriter(characterFile))) {
                new Gson().toJson(characterStory, writer);
            }
            updateCharacterSceneInkFile(oldName, newName);
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't update character {} file! {}", oldName, e.getStackTrace());
            return false;
        }
    }

    public static boolean updateCharacterFolder(CharacterStory characterStory) {
        File characterFolderNew = new File(characterDirectory, Utils.getSnakeCase(characterStory.getName()));
        File characterFile = new File(characterFolderNew, "data" + EXTENSION_DATA_FILE);
        File saveFile = new File(savesDirectory, "save" + EXTENSION_DATA_FILE);
        try {
            List<Chapter> chapters = NarrativeCraftMod.getInstance().getChapterManager().getChapters();
            for(Chapter chapter : chapters) {
                for(Scene scene : chapter.getSceneList()) {
                    for(Animation animation : scene.getAnimationList()) {
                        if(animation.getCharacter().getName().equalsIgnoreCase(characterStory.getName())) {
                            animation.setCharacter(characterStory);
                            updateAnimationFile(animation);
                        }
                    }
                    for(CameraAngleGroup cameraAngleGroup : scene.getCameraAngleGroupList()) {
                        for(CharacterStoryData characterStoryData : cameraAngleGroup.getCharacterStoryDataList()) {
                            if(characterStoryData.getCharacterStory().getName().equalsIgnoreCase(characterStory.getName())) {
                                characterStoryData.setCharacterStory(characterStory);
                                updateCameraAnglesFile(scene);
                            }
                        }
                    }
                }
            }
            StorySave save = getSave();
            if(save != null) {
                for(CharacterStoryData characterStoryData : save.getCharacterStoryDataList()) {
                    if(characterStoryData.getCharacterStory().getName().equalsIgnoreCase(characterStory.getName())) {
                        characterStoryData.setCharacterStory(characterStory);
                    }
                }
                try(Writer writer = new BufferedWriter(new FileWriter(saveFile))) {
                    new Gson().toJson(save, writer);
                }
            }
            try(Writer writer = new BufferedWriter(new FileWriter(characterFile))) {
                new Gson().toJson(characterStory, writer);
            }
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't update character {} file! {}", characterStory.getName(), e.getStackTrace());
            return false;
        }
    }

    public static boolean updateNpcSceneFolder(String oldName, String newName, Scene scene) {
        File sceneFolder = getSceneFolder(scene);
        File dataFile = new File(sceneFolder, "data");
        File npcFolder = new File(dataFile, "npc");
        CharacterStory characterStory = scene.getNpc(newName);
        File characterFolderNew = new File(npcFolder, Utils.getSnakeCase(newName));
        File characterFolderOld = new File(npcFolder, Utils.getSnakeCase(oldName));
        File characterFile = new File(characterFolderNew, "data" + EXTENSION_DATA_FILE);
        File saveFile = new File(savesDirectory, "save" + EXTENSION_DATA_FILE);
        try {
            Files.move(characterFolderOld.toPath(), characterFolderNew.toPath());
            for(Animation animation : scene.getAnimationList()) {
                if(animation.getCharacter().getName().equalsIgnoreCase(oldName) || animation.getCharacter().getName().equalsIgnoreCase(newName)) {
                    animation.setCharacter(characterStory);
                    updateAnimationFile(animation);
                }
            }
            for(CameraAngleGroup cameraAngleGroup : scene.getCameraAngleGroupList()) {
                for(CharacterStoryData characterStoryData : cameraAngleGroup.getCharacterStoryDataList()) {
                    if(characterStoryData.getCharacterStory().getName().equalsIgnoreCase(oldName) || characterStoryData.getCharacterStory().getName().equalsIgnoreCase(newName)) {
                        characterStoryData.setCharacterStory(characterStory);
                        updateCameraAnglesFile(scene);
                    }
                }
            }
            StorySave save = getSave();
            if(save != null) {
                for(CharacterStoryData characterStoryData : save.getCharacterStoryDataList()) {
                    if(characterStoryData.getCharacterStory().getName().equalsIgnoreCase(oldName)) {
                        characterStoryData.getCharacterStory().setName(newName);
                    }
                }
                try(Writer writer = new BufferedWriter(new FileWriter(saveFile))) {
                    new Gson().toJson(save, writer);
                }
            }
            try(Writer writer = new BufferedWriter(new FileWriter(characterFile))) {
                new Gson().toJson(characterStory, writer);
            }
            updateCharacterSceneInkFile(oldName, newName);
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't update npc {} folder! {}", oldName, e.getStackTrace());
            return false;
        }
    }

    public static boolean updateNpcSceneFolder(CharacterStory characterStory, Scene scene) {
        File sceneFolder = getSceneFolder(scene);
        File dataFile = new File(sceneFolder, "data");
        File npcFolder = new File(dataFile, "npc");
        File characterFolder = new File(npcFolder, Utils.getSnakeCase(characterStory.getName()));
        File characterFile = new File(characterFolder, "data" + EXTENSION_DATA_FILE);
        File saveFile = new File(savesDirectory, "save" + EXTENSION_DATA_FILE);
        try {
            for(Animation animation : scene.getAnimationList()) {
                if(animation.getCharacter().getName().equalsIgnoreCase(characterStory.getName())) {
                    animation.setCharacter(characterStory);
                    updateAnimationFile(animation);
                }
            }
            for(CameraAngleGroup cameraAngleGroup : scene.getCameraAngleGroupList()) {
                for(CharacterStoryData characterStoryData : cameraAngleGroup.getCharacterStoryDataList()) {
                    if(characterStoryData.getCharacterStory().getName().equalsIgnoreCase(characterStory.getName())) {
                        characterStoryData.setCharacterStory(characterStory);
                        updateCameraAnglesFile(scene);
                    }
                }
            }
            StorySave save = getSave();
            if(save != null) {
                for(CharacterStoryData characterStoryData : save.getCharacterStoryDataList()) {
                    if(characterStoryData.getCharacterStory().getName().equalsIgnoreCase(characterStory.getName())) {
                        characterStoryData.setCharacterStory(characterStory);
                    }
                }
                try(Writer writer = new BufferedWriter(new FileWriter(saveFile))) {
                    new Gson().toJson(save, writer);
                }
            }
            try(Writer writer = new BufferedWriter(new FileWriter(characterFile))) {
                new Gson().toJson(characterStory, writer);
            }
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't update npc {} folder! {}", characterStory.getName(), e.getStackTrace());
            return false;
        }
    }

    public static boolean createCharacterFile(CharacterStory characterStory) {
        File characterFolder = createDirectory(characterDirectory, getSnakeCaseName(characterStory.getName()));
        File characterFile = createFile(characterFolder, "data" + EXTENSION_DATA_FILE);
        File skinsFolder = createDirectory(characterFolder, "skins");
        PlayerSkin defaultPlayerSkin = DefaultPlayerSkin.get(UUID.randomUUID());
        characterStory.setModel(defaultPlayerSkin.model());
        File mainSkinFile = createFile(skinsFolder, "main.png");
        try(InputStream inputStream = Minecraft.getInstance().getResourceManager().open(defaultPlayerSkin.texture())) {
            Files.copy(inputStream, mainSkinFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {} // Don't really need to handle

        Gson gson = new GsonBuilder().create();
        try(Writer writer = new BufferedWriter(new FileWriter(characterFile))) {
            gson.toJson(characterStory, writer);
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't create character {} file! {}", characterStory.getName(), e.getStackTrace());
            return false;
        }
    }

    public static boolean createCharacterFileScene(CharacterStory characterStory, Scene scene) {
        File sceneFile = getSceneFolder(scene);
        File sceneDataFolder = new File(sceneFile, "data");
        File npcFolder = new File(sceneDataFolder, "npc");
        File characterFolder = createDirectory(npcFolder, Utils.getSnakeCase(characterStory.getName()));
        File characterFile = createFile(characterFolder, "data" + EXTENSION_DATA_FILE);
        PlayerSkin defaultPlayerSkin = DefaultPlayerSkin.get(UUID.randomUUID());
        characterStory.setModel(defaultPlayerSkin.model());
        File mainSkinFile = createFile(characterFolder, "main.png");
        try(InputStream inputStream = Minecraft.getInstance().getResourceManager().open(defaultPlayerSkin.texture())) {
            Files.copy(inputStream, mainSkinFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {} // Don't really need to handle
        characterStory.getCharacterSkinController().setCurrentSkin(new File(npcFolder, "main.png"));
        Gson gson = new GsonBuilder().create();
        try(Writer writer = new BufferedWriter(new FileWriter(characterFile))) {
            gson.toJson(characterStory, writer);
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't create npc {} folder! {}", characterStory.getName(), e.getStackTrace());
            return false;
        }
    }

    public static void removeCharacterFolder(CharacterStory characterStory) {
        deleteDirectory(new File(characterDirectory, Utils.getSnakeCase(characterStory.getName())));
    }

    public static void removeNpcFolder(CharacterStory characterStory) {
        File sceneFolder = getSceneFolder(characterStory.getScene());
        File dataFolder = new File(sceneFolder, "data");
        File npcFolder = new File(dataFolder, "npc");
        deleteDirectory(new File(npcFolder, Utils.getSnakeCase(characterStory.getName())));
    }

    public static void removeChapterFolder(Chapter chapter) {
        File chapterFolder = new File(chaptersDirectory, String.valueOf(chapter.getIndex()));
        deleteDirectory(chapterFolder);
        updateMainInkFile();
    }

    public static void removeSceneFolder(Scene scene) {
        File chapterFolder = new File(chaptersDirectory, String.valueOf(scene.getChapter().getIndex()));
        File scenesFolder = new File(chapterFolder, SCENES_DIRECTORY_NAME);
        File sceneFolder = new File(scenesFolder, getSnakeCaseName(scene.getName()));
        deleteDirectory(sceneFolder);
        updateMainInkFile();
    }

    public static void removeAnimationFileFromScene(Animation animation) {
        File dataFolder = getDataFolderOfScene(animation.getScene());
        File animationsFolder = new File(dataFolder, "animations");
        File animationFile = new File(animationsFolder, getSnakeCaseName(animation.getName()) + EXTENSION_DATA_FILE);
        animationFile.delete();
    }

    public static boolean subscenesFileExist(Scene scene) {
        File dataFolder = getDataFolderOfScene(scene);
        return new File(dataFolder, "subscenes" + EXTENSION_DATA_FILE).exists();
    }

    public static boolean cutscenesFileExist(Scene scene) {
        File dataFolder = getDataFolderOfScene(scene);
        return new File(dataFolder, "cutscenes" + EXTENSION_DATA_FILE).exists();
    }

    public static boolean animationFileExist(Scene scene, Animation animation) {
        File dataFolder = getDataFolderOfScene(scene);
        File animationsFolder = new File(dataFolder, "animations");
        return new File(animationsFolder, getSnakeCaseName(animation.getName()) + EXTENSION_DATA_FILE).exists();
    }

    public static boolean animationFileExist(Scene scene, String animationName) {
        File dataFolder = getDataFolderOfScene(scene);
        File animationsFolder = new File(dataFolder, "animations");
        return new File(animationsFolder, getSnakeCaseName(animationName) + EXTENSION_DATA_FILE).exists();
    }

    public static File animationFolder(Scene scene) {
        File dataFolder = getDataFolderOfScene(scene);
        return new File(dataFolder, "animations");
    }

    public static String getStoryFile() throws IOException {
        File buildFolder = new File(mainDirectory, "build");
        return Files.readString(new File(buildFolder, "story.json").toPath());
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
        File chapterFolder = new File(chaptersDirectory, String.valueOf(scene.getChapter().getIndex()));
        File scenesFolder = new File(chapterFolder, SCENES_DIRECTORY_NAME);
        File sceneFolder = new File(scenesFolder, scene.getSnakeCase());
        return new File(sceneFolder, scene.getSnakeCase() + EXTENSION_SCRIPT_FILE);
    }

    public static File getSceneFolder(Scene scene) {
        File chapterFolder = new File(chaptersDirectory, String.valueOf(scene.getChapter().getIndex()));
        File scenesFolder = new File(chapterFolder, SCENES_DIRECTORY_NAME);
        return new File(scenesFolder, scene.getSnakeCase());
    }

    public static void updateMainInkFile() {
        List<Chapter> chapterList = NarrativeCraftMod.getInstance().getChapterManager().getChapters();
        StringBuilder stringBuilder = new StringBuilder();
        String chapterPath = CHAPTERS_DIRECTORY_NAME + "\\";
        String scenesPath = SCENES_DIRECTORY_NAME + "\\";
        for(Chapter chapter : chapterList) {
            stringBuilder.append("INCLUDE ")
                    .append(chapterPath)
                    .append(chapter.getIndex())
                    .append("\\")
                    .append("chapter_")
                    .append(chapter.getIndex())
                    .append(EXTENSION_SCRIPT_FILE)
                    .append("\n");
            for(Scene scene : chapter.getSceneList()) {
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
        try(Writer writer = new BufferedWriter(new FileWriter(mainInkFile))) {
            writer.write(stringBuilder.toString());
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Can't update main ink file! {}", e.getStackTrace());
            throw new RuntimeException("Can't update main ink file! ", e);
        }
    }

    public static boolean writeSave(StoryHandler storyHandler) {
       try {
           File saveFile = new File(savesDirectory, "save.json");
           StorySave save = new StorySave(storyHandler);
           try(Writer writer = new BufferedWriter(new FileWriter(saveFile))) {
               new Gson().toJson(save, writer);
           }
           return true;
       } catch (Exception e) {
           throw new RuntimeException(e);
       }
    }

    public static StorySave getSave() {
        File saveFile = new File(savesDirectory, "save" + EXTENSION_DATA_FILE);
        if(!saveFile.exists()) return null;
        try {
            String saveContent = Files.readString(saveFile.toPath());
            StorySave save = new Gson().fromJson(saveContent, StorySave.class);
            Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getChapterByIndex(save.getChapterIndex());
            Scene scene = chapter.getSceneByName(save.getSceneName());
            for(CharacterStoryData characterStoryData : save.getCharacterStoryDataList()) {
                CharacterStory characterStory = null;
                if(characterStoryData.getCharacterStory().getCharacterType() == CharacterStory.CharacterType.MAIN) {
                    characterStory = NarrativeCraftMod.getInstance().getCharacterManager().getCharacter(characterStoryData.getCharacterStory().getName());
                    characterStoryData.setCharacterStory(characterStory);
                } else if(characterStoryData.getCharacterStory().getCharacterType() == CharacterStory.CharacterType.NPC) {
                    characterStory = scene.getNpc(characterStoryData.getCharacterStory().getName());
                }
                characterStoryData.setCharacterStory(characterStory);
            }
            return save;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getChapterSceneSneakCase(Scene scene) {
        return "chapter_" + scene.getChapter().getIndex() + "_" + getSnakeCaseName(scene.getName());
    }

    private static String getChapterSceneSneakCase(int chapterIndex, String sceneName) {
        return "chapter_" + chapterIndex + "_" + getSnakeCaseName(sceneName);
    }

    private static String getKnotSceneName(Scene scene) {
        return "=== " + getChapterSceneSneakCase(scene) + " ===";
    }

    private static String getSnakeCaseName(String name) {
        return String.join("_", name.toLowerCase().split(" "));
    }

    private static File getDataFolderOfScene(Scene scene) {
        File chapterFolder = new File(chaptersDirectory, String.valueOf(scene.getChapter().getIndex()));
        File scenesFolder = new File(chapterFolder, SCENES_DIRECTORY_NAME);
        File sceneFolder = new File(scenesFolder, getSnakeCaseName(scene.getName()));
        return new File(sceneFolder, "data");
    }

    private static File createDirectory(File parent, String name) {
        File directory = new File(parent, name);
        if(!directory.exists()) {
            if(!directory.mkdir()) NarrativeCraftMod.LOG.error("Couldn't create directory {}!", name);
        }
        return directory;
    }

    private static File createFile(File parent, String name) {
        File file = new File(parent, name);
        if(!file.exists()) {
            try {
                if(!file.createNewFile()) NarrativeCraftMod.LOG.error("Couldn't create file {}!", file.getAbsolutePath());
            } catch (IOException e) {
                NarrativeCraftMod.LOG.error("Couldn't create file {}! Cause: {}", file.getAbsolutePath(), e.getStackTrace());
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
        File characterFolder = new File(characterDirectory, getSnakeCaseName(characterStory.getName()));
        File skinsFolder = new File(characterFolder, "skins");
        File skin = new File(skinsFolder, name);
        if(skin.exists()) {
            return skin;
        } else {
            return null;
        }
    }
}
