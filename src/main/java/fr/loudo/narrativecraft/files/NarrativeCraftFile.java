package fr.loudo.narrativecraft.files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.loudo.narrativecraft.NarrativeCraft;
import fr.loudo.narrativecraft.narrative.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.scenes.Scene;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.*;

public class NarrativeCraftFile {

    private static final String DIRECTORY_NAME = NarrativeCraft.MODID;
    private static final String CHAPTER_DIRECTORY_NAME = "chapters";
    private static final String SCENE_DIRECTORY_NAME = "scenes";
    private static final String ANIMATION_DIRECTORY_NAME = "animations";
    private static final String CHARACTER_DIRECTORY_NAME = "characters";
    private static final String EXTENSTION_FILE = ".json";

    public static File mainDirectory;
    public static File chapterDirectory;
    public static File sceneDirectory;
    public static File characterDirectory;

    public static void init(MinecraftServer server) {
        mainDirectory = createDirectory(server.getWorldPath(LevelResource.ROOT).toFile(), DIRECTORY_NAME);
        chapterDirectory = createDirectory(mainDirectory, CHAPTER_DIRECTORY_NAME);
        sceneDirectory = createDirectory(mainDirectory, SCENE_DIRECTORY_NAME);
        characterDirectory = createDirectory(mainDirectory, CHARACTER_DIRECTORY_NAME);
    }

    public static void saveChapter(Chapter chapter) throws IOException {
        File file = createFile(chapterDirectory, String.valueOf(chapter.getIndex()));
        save(chapter, file, true);
    }

    public static void saveScene(Scene scene) throws IOException {
        File chapterDirectoryOfScene = createDirectory(sceneDirectory, String.valueOf(scene.getChapter().getIndex()));
        File file = createFile(chapterDirectoryOfScene, scene.getName().toLowerCase());
        save(scene, file, true);
    }

    public static void saveAnimation(Animation animation) throws IOException {
        String chapterDirectoryName = String.valueOf(animation.getScene().getChapter().getIndex());
        File sceneDirectoryOfAnimation = createDirectory(new File(sceneDirectory, chapterDirectoryName), ANIMATION_DIRECTORY_NAME);
        File file = createFile(sceneDirectoryOfAnimation, animation.getName().toLowerCase());
        save(animation, file, false);
    }

    private static void save(Object element, File file, boolean prettyString) throws IOException {
        Gson gson;
        if(prettyString) {
            gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();
        } else {
            gson = new GsonBuilder()
                    .create();
        }
        try(Writer writer = new BufferedWriter(new FileWriter(file))) {
            gson.toJson(element, writer);
        }
    }

    private static File createDirectory(File parent, String name) {
        File directory = new File(parent, name);
        if(!directory.exists()) {
            if(!directory.mkdir()) NarrativeCraft.LOGGER.warn("Couldn't create directory {}!", name);
        }
        return directory;
    }

    private static File createFile(File parent, String name) {
        File file = new File(parent, name + EXTENSTION_FILE);
        if(!file.exists()) {
            try {
                if(!file.createNewFile()) NarrativeCraft.LOGGER.warn("Couldn't create file {}!", file.getAbsoluteFile());
            } catch (IOException e) {
                throw new RuntimeException("Couldn't create file: " + e);
            }
        }
        return file;
    }


}
