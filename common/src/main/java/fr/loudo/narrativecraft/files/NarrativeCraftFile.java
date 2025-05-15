package fr.loudo.narrativecraft.files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.*;
import java.nio.file.Files;

public class NarrativeCraftFile {

    public static final String EXTENSION_SCRIPT_FILE = ".ink";
    public static final String EXTENSION_DATA_FILE = ".json";

    private static final String DIRECTORY_NAME = NarrativeCraftMod.MOD_ID;

    private static final String BUILD_DIRECTORY_NAME = "build";
    private static final String CHAPTERS_DIRECTORY_NAME = "chapters";
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
            new File(chapterFolder, "scenes").mkdir();
            File detailsFile = getDetailsFile(chapterFolder);
            detailsFile.createNewFile();
            String content = String.format("{\"name\":\"%s\",\"description\":\"%s\"}", chapter.getName(), chapter.getDescription());
            try(Writer writer = new BufferedWriter(new FileWriter(detailsFile))) {
                writer.write(content);
            }
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't create details file! " + e.getMessage());
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
        File scenesFolder = new File(chapterFolder.getAbsoluteFile(), "scenes");
        File sceneFolder = new File(scenesFolder.getAbsoluteFile(), String.join("_", scene.getName().toLowerCase().split(" ")));
        sceneFolder.mkdir();
        File dataFolder = new File(sceneFolder.getAbsoluteFile(), "data");
        dataFolder.mkdir();
        try {
            File detailsFile = getDetailsFile(dataFolder);
            detailsFile.createNewFile();
            new File(sceneFolder.getAbsoluteFile(), getCamelCaseName(scene.getName()) + EXTENSION_SCRIPT_FILE).createNewFile();
            new File(dataFolder.getAbsoluteFile(), "animations").mkdir();
            new File(dataFolder.getAbsoluteFile(), "cutscenes" + EXTENSION_DATA_FILE).createNewFile();
            new File(dataFolder.getAbsoluteFile(), "subscenes" + EXTENSION_DATA_FILE).createNewFile();
            String content = String.format("{\"name\":\"%s\",\"description\":\"%s\"}", scene.getName(), scene.getDescription());
            try(Writer writer = new BufferedWriter(new FileWriter(detailsFile))) {
                writer.write(content);
            }
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.error("Couldn't create details file! " + e.getMessage());
            return false;
        }

    }

    public static boolean updateSceneDetails(Scene scene, String name, String description) {
        File chapterFolder = new File(chaptersDirectory, String.valueOf(scene.getChapter().getIndex()));
        File scenesFolder = new File(chapterFolder, "scenes");
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

    public static void removeChapterFolder(Chapter chapter) {
        File chapterFolder = new File(chaptersDirectory, String.valueOf(chapter.getIndex()));
        deleteDirectory(chapterFolder);
    }

    public static void removeSceneFolder(Scene scene) {
        File chapterFolder = new File(chaptersDirectory, String.valueOf(scene.getChapter().getIndex()));
        File scenesFolder = new File(chapterFolder, "scenes");
        File sceneFolder = new File(scenesFolder, getCamelCaseName(scene.getName()));
        deleteDirectory(sceneFolder);
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

    private static String getCamelCaseName(String name) {
        return String.join("_", name.toLowerCase().split(" "));
    }

    private static File getDataFolderOfScene(Scene scene) {
        File chapterFolder = new File(chaptersDirectory, String.valueOf(scene.getChapter().getIndex()));
        File scenesFolder = new File(chapterFolder, "scenes");
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
