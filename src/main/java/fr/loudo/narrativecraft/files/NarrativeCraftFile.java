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
import java.util.ArrayList;
import java.util.List;

public class NarrativeCraftFile {

    private static final String DIRECTORY_NAME = NarrativeCraft.MODID;
    private static final String CHAPTER_DIRECTORY_NAME = "chapters";
    private static final String CHARACTER_DIRECTORY_NAME = "characters";
    private static final String EXTENSTION_FILE = ".json";

    public static File mainDirectory;
    public static File chapterDirectory;
    public static File characterDirectory;

    public static void init(MinecraftServer server) {
        mainDirectory = createDirectory(server.getWorldPath(LevelResource.ROOT).toFile(), DIRECTORY_NAME);
        chapterDirectory = createDirectory(mainDirectory, CHAPTER_DIRECTORY_NAME);
        characterDirectory = createDirectory(mainDirectory, CHARACTER_DIRECTORY_NAME);

        NarrativeCraft.getChapterManager().setChapters(getChaptersFromDirectory());
    }

    public static void saveChapter(Chapter chapter) throws IOException {
        File file = createFile(chapterDirectory, String.valueOf(chapter.getIndex()));
        save(chapter, file);
    }

    //TODO: method to load chapter individually, for better performance. for instance if a player player play first chapter, then only deserialize first chapter.
    public static List<Chapter> getChaptersFromDirectory() {
        List<Chapter> finalList = new ArrayList<>();
        File directory = NarrativeCraftFile.chapterDirectory;
        File[] files = directory.listFiles();
        if(files == null) {
            NarrativeCraft.LOGGER.warn("Couldn't retrieve chapters file!");
            return finalList;
        }
        Gson gson = new GsonBuilder().create();
        for(File file : files) {
            try(Reader reader = new BufferedReader(new FileReader(file))) {
                Chapter chapter = gson.fromJson(reader, Chapter.class);
                for(Scene scene : chapter.getScenes()) {
                    scene.setChapter(chapter);
                    for(Animation animation : scene.getAnimations()) {
                        animation.setScene(scene);
                    }
                }
                finalList.add(chapter);
            } catch (IOException e) {
                NarrativeCraft.LOGGER.warn("File {} couldn't be opened", file.getName());
            }
        }

        return finalList;
    }

    private static void save(Object element, File file) throws IOException {
        Gson gson = new GsonBuilder().create();
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
