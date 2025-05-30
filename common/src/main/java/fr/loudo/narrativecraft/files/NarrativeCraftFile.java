package fr.loudo.narrativecraft.files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

    public static File mainDirectory;
    public static File chaptersDirectory;
    public static File characterDirectory;
    public static File savesDirectory;
    public static File buildDirectory;
    public static File mainInkFile;

    public static void init(MinecraftServer server) {
        mainDirectory = createDirectory(server.getWorldPath(LevelResource.ROOT).toFile(), DIRECTORY_NAME);
        chaptersDirectory = createDirectory(mainDirectory, CHAPTERS_DIRECTORY_NAME);
        characterDirectory = createDirectory(mainDirectory, CHARACTERS_DIRECTORY_NAME);
        savesDirectory = createDirectory(mainDirectory, SAVES_DIRECTORY_NAME);
        buildDirectory = createDirectory(mainDirectory, BUILD_DIRECTORY_NAME);
        mainInkFile = createFile(mainDirectory, MAIN_INK_NAME);
    }

    public static File getDetailsFile(File file) {
        return new File(file.getAbsoluteFile(), "details" + NarrativeCraftFile.EXTENSION_DATA_FILE);
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
            NarrativeCraftMod.LOG.error("Couldn't create chapter!  " + e.getMessage());
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
            NarrativeCraftMod.LOG.error("Couldn't update chapter {} details file! {}", chapter.getIndex(), e.getMessage());
            return false;
        }
    }

    public static boolean createSceneFolder(Scene scene) {
        File chapterFolder = createDirectory(chaptersDirectory, String.valueOf(scene.getChapter().getIndex()));
        if(!chapterFolder.exists()) createChapterDirectory(scene.getChapter());
        File scenesFolder = new File(chapterFolder.getAbsoluteFile(), SCENES_DIRECTORY_NAME);
        File sceneFolder = new File(scenesFolder.getAbsoluteFile(), getCamelCaseName(scene.getName()));
        if(sceneFolder.exists()) return true;
        sceneFolder.mkdir();
        File dataFolder = new File(sceneFolder.getAbsoluteFile(), "data");
        dataFolder.mkdir();
        try {
            File detailsFile = getDetailsFile(dataFolder);
            detailsFile.createNewFile();
            File sceneScriptFile = new File(sceneFolder.getAbsoluteFile(), getCamelCaseName(scene.getName()) + EXTENSION_SCRIPT_FILE);
            sceneScriptFile.createNewFile();
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
            NarrativeCraftMod.LOG.error("Couldn't create scene! {}", e.getMessage());
            return false;
        }

    }

    public static boolean updateSceneDetails(Scene scene, String name, String description) {
        File chapterFolder = new File(chaptersDirectory, String.valueOf(scene.getChapter().getIndex()));
        File scenesFolder = new File(chapterFolder, SCENES_DIRECTORY_NAME);
        File sceneFolder = new File(scenesFolder, getCamelCaseName(scene.getName()));
        try {
            File newSceneFolder = new File(scenesFolder, getCamelCaseName(name));
            if(!scene.getName().equals(name)) {
                Files.move(sceneFolder.toPath(), newSceneFolder.toPath());
                sceneFolder = newSceneFolder;
            }
            File sceneDetails = getDetailsFile(new File(sceneFolder, "data"));
            String content = String.format("{\"name\":\"%s\",\"description\":\"%s\"}", name, description);
            try(Writer writer = new BufferedWriter(new FileWriter(sceneDetails))) {
                writer.write(content);
            }
            File scriptFile = new File(sceneFolder, getCamelCaseName(scene.getName()) + EXTENSION_SCRIPT_FILE);
            scriptFile.renameTo(new File(sceneFolder, getCamelCaseName(name) + EXTENSION_SCRIPT_FILE));
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't update scene {} details file of chapter {}! {}", scene.getName(), scene.getChapter().getIndex(), e.getMessage());
            return false;
        }
    }

    public static boolean updateKnotSceneNameFromChapter(Chapter chapter, String oldName, String newName) {
        File chapterFolder = new File(chaptersDirectory, String.valueOf(chapter.getIndex()));
        File scenesFolder = new File(chapterFolder, SCENES_DIRECTORY_NAME);
        try {
            for(Scene scene : chapter.getSceneList()) {
                File sceneFolder = new File(scenesFolder, getCamelCaseName(scene.getName()));
                File scriptFile = new File(sceneFolder, getCamelCaseName(scene.getName()) + EXTENSION_SCRIPT_FILE);
                String scriptContent = Files.readString(scriptFile.toPath());
                if(scriptContent.contains(NarrativeCraftFile.getChapterSceneCamelCase(chapter.getIndex(), oldName))) {
                    scriptContent = scriptContent.replace(getChapterSceneCamelCase(chapter.getIndex(), oldName), getChapterSceneCamelCase(chapter.getIndex(), newName));
                    try(Writer writer = new BufferedWriter(new FileWriter(scriptFile))) {
                        writer.write(scriptContent);
                    }
                }
            }
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't update knot scen name file of chapter {} ! {}", chapter.getIndex(), e.getMessage());
            return false;
        }
    }

    public static boolean updateCutsceneFile(Scene scene) {
        File dataFolder = getDataFolderOfScene(scene);
        File cutsceneFile = new File(dataFolder, "cutscenes" + EXTENSION_DATA_FILE);
        Gson gson = new GsonBuilder().create();
        try(Writer writer = new BufferedWriter(new FileWriter(cutsceneFile))) {
            gson.toJson(scene.getCutsceneList(), writer);
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't update cutscenes file of scene {} of chapter {} ! {}", scene.getName(), scene.getChapter().getIndex(), e.getMessage());
            return false;
        }
    }

    public static boolean updateSubsceneFile(Scene scene) {
        File dataFolder = getDataFolderOfScene(scene);
        File subscenesFile = new File(dataFolder, "subscenes" + EXTENSION_DATA_FILE);
        Gson gson = new GsonBuilder().create();
        try(Writer writer = new BufferedWriter(new FileWriter(subscenesFile))) {
            gson.toJson(scene.getSubsceneList(), writer);
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't update subscenes file of scene {} of chapter {} ! {}", scene.getName(), scene.getChapter().getIndex(), e.getMessage());
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
            NarrativeCraftMod.LOG.error("Couldn't update camera angles file of scene {} of chapter {} ! {}", scene.getName(), scene.getChapter().getIndex(), e.getMessage());
            return false;
        }
    }

    public static boolean updateAnimationFile(Animation animation) {
        File dataFolder = getDataFolderOfScene(animation.getScene());
        File animationFolder = new File(dataFolder, "animations");
        if(!animationFolder.exists()) animationFolder.mkdir();
        File animationFile = createFile(animationFolder, getCamelCaseName(animation.getName()) + EXTENSION_DATA_FILE);
        Gson gson = new GsonBuilder().create();
        try(Writer writer = new BufferedWriter(new FileWriter(animationFile))) {
            gson.toJson(animation, writer);
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't update animation {} file of scene {} of chapter {} ! {}", animation.getName(), animation.getScene().getName(), animation.getScene().getChapter().getIndex(), e.getMessage());
            return false;
        }
    }

    public static boolean updateCharacterFile(CharacterStory characterStory) {
        File animationFile = createFile(characterDirectory, getCamelCaseName(characterStory.getName()) + EXTENSION_DATA_FILE);
        Gson gson = new GsonBuilder().create();
        try(Writer writer = new BufferedWriter(new FileWriter(animationFile))) {
            gson.toJson(characterStory, writer);
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't update character {} file! {}", characterStory.getName(), e.getMessage());
            return false;
        }
    }

    public static void removeCharacterFile(CharacterStory characterStory) {
        new File(characterDirectory, getCamelCaseName(characterStory.getName()) + EXTENSION_DATA_FILE).delete();
    }

    public static void removeChapterFolder(Chapter chapter) {
        File chapterFolder = new File(chaptersDirectory, String.valueOf(chapter.getIndex()));
        deleteDirectory(chapterFolder);
        updateMainInkFile();
    }

    public static void removeSceneFolder(Scene scene) {
        File chapterFolder = new File(chaptersDirectory, String.valueOf(scene.getChapter().getIndex()));
        File scenesFolder = new File(chapterFolder, SCENES_DIRECTORY_NAME);
        File sceneFolder = new File(scenesFolder, getCamelCaseName(scene.getName()));
        deleteDirectory(sceneFolder);
        updateMainInkFile();
    }

    public static void removeAnimationFileFromScene(Animation animation) {
        File dataFolder = getDataFolderOfScene(animation.getScene());
        File animationsFolder = new File(dataFolder, "animations");
        File animationFile = new File(animationsFolder, getCamelCaseName(animation.getName()) + EXTENSION_DATA_FILE);
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
        return new File(animationsFolder, getCamelCaseName(animation.getName()) + EXTENSION_DATA_FILE).exists();
    }

    public static boolean animationFileExist(Scene scene, String animationName) {
        File dataFolder = getDataFolderOfScene(scene);
        File animationsFolder = new File(dataFolder, "animations");
        return new File(animationsFolder, getCamelCaseName(animationName) + EXTENSION_DATA_FILE).exists();
    }

    public static String getStoryFile() throws IOException {
        File buildFolder = new File(mainDirectory, "build");
        return Files.readString(new File(buildFolder, "story.json").toPath());
    }

    public static List<String> readSceneLines(Scene scene) {
        File sceneScript = getSceneFile(scene);
        try {
            return Arrays.stream(Files.readString(sceneScript.toPath()).split("\n")).toList();
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public static File getSceneFile(Scene scene) {
        File chapterFolder = new File(chaptersDirectory, String.valueOf(scene.getChapter().getIndex()));
        File scenesFolder = new File(chapterFolder, SCENES_DIRECTORY_NAME);
        File sceneFolder = new File(scenesFolder, scene.getCamelCase());
        return new File(sceneFolder, scene.getCamelCase() + EXTENSION_SCRIPT_FILE);
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
                        .append(getCamelCaseName(scene.getName()))
                        .append("\\")
                        .append(getCamelCaseName(scene.getName()))
                        .append(EXTENSION_SCRIPT_FILE)
                        .append("\n");
            }
        }
        stringBuilder.append("\n").append("-> chapter_1");
        try(Writer writer = new BufferedWriter(new FileWriter(mainInkFile))) {
            writer.write(stringBuilder.toString());
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Can't update main ink file! {}", e.getMessage());
            throw new RuntimeException("Can't update main ink file! ", e);
        }
    }

    public static String getChapterSceneCamelCase(Scene scene) {
        return "chapter_" + scene.getChapter().getIndex() + "_" + getCamelCaseName(scene.getName());
    }

    private static String getChapterSceneCamelCase(int chapterIndex, String sceneName) {
        return "chapter_" + chapterIndex + "_" + getCamelCaseName(sceneName);
    }

    private static String getKnotSceneName(Scene scene) {
        return "=== " + getChapterSceneCamelCase(scene) + " ===";
    }

    private static String getCamelCaseName(String name) {
        return String.join("_", name.toLowerCase().split(" "));
    }

    private static File getDataFolderOfScene(Scene scene) {
        File chapterFolder = new File(chaptersDirectory, String.valueOf(scene.getChapter().getIndex()));
        File scenesFolder = new File(chapterFolder, SCENES_DIRECTORY_NAME);
        File sceneFolder = new File(scenesFolder, getCamelCaseName(scene.getName()));
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
                NarrativeCraftMod.LOG.error("Couldn't create file {}! Cause: {}", file.getAbsolutePath(), e.getMessage());
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
}
