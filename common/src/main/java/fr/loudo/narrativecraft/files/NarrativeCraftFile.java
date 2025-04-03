package fr.loudo.narrativecraft.files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.character.Character;
import fr.loudo.narrativecraft.narrative.recordings.actions.Action;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionDeserializer;
import fr.loudo.narrativecraft.narrative.scenes.Scene;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class NarrativeCraftFile {

    private static final String DIRECTORY_NAME = NarrativeCraftMod.MOD_ID;
    private static final String CHAPTER_DIRECTORY_NAME = "chapters";
    private static final String CHARACTER_DIRECTORY_NAME = "characters";
    private static final String ANIMATION_DIRECTORY_NAME = "animations";
    private static final String EXTENSTION_FILE = ".json";

    public static File mainDirectory;
    public static File chapterDirectory;
    public static File animationDirectory;
    public static File characterDirectory;

    public static void init(MinecraftServer server) {
        mainDirectory = createDirectory(server.getWorldPath(LevelResource.ROOT).toFile(), DIRECTORY_NAME);
        chapterDirectory = createDirectory(mainDirectory, CHAPTER_DIRECTORY_NAME);
        animationDirectory = createDirectory(mainDirectory, ANIMATION_DIRECTORY_NAME);
        characterDirectory = createDirectory(mainDirectory, CHARACTER_DIRECTORY_NAME);

        NarrativeCraftMod.getInstance().getChapterManager().setChapters(getChaptersFromDirectory());
        NarrativeCraftMod.getInstance().getCharacterManager().setCharacters(getCharactersFromDirectory());
    }

    public static void saveChapter(Chapter chapter) throws IOException {
        File file = createFile(chapterDirectory, String.valueOf(chapter.getIndex()));
        save(chapter, file);
    }

    public static void removeChapter(Chapter chapter) throws IOException {
        File file = createFile(chapterDirectory, String.valueOf(chapter.getIndex()));
        if(!file.delete()) {
            NarrativeCraftMod.LOG.warn("Couldn't remove chapter file " + file.getName() + ".");
        }
        for(Scene scene : chapter.getScenes()) {
            removeAnimationsFileByScene(scene);
        }
    }

    public static void removeAnimationsFileByScene(Scene scene) throws IOException {
        for(String animationFileName : scene.getAnimationFilesName()) {
            removeAnimationFile(animationFileName);
        }
    }

    public static boolean removeAnimationFile(String animationName) throws IOException {
        File fileAnim = new File(animationDirectory, animationName + EXTENSTION_FILE);
        return fileAnim.delete();
    }

    public static void saveAnimation(Animation animation) throws IOException {
        File file = createFile(animationDirectory, animation.getName().toLowerCase());
        animation.getScene().addAnimation(animation.getName().toLowerCase());
        saveChapter(animation.getScene().getChapter());
        save(animation, file);
    }

    public static void saveCharacter(Character character) throws IOException {
        File file = createFile(characterDirectory, character.getName().toLowerCase());
        save(character, file, true);
    }

    public static boolean animationFileExists(String animationName) {
        return new File(animationDirectory, animationName + EXTENSTION_FILE).exists();
    }

    public static Animation getAnimationFromFile(String animationName) {
        File file = new File(animationDirectory, animationName + EXTENSTION_FILE);
        if(file.exists()) {
            Gson gson = new GsonBuilder().registerTypeAdapter(Action.class, new ActionDeserializer()).create();
            try(Reader reader = new BufferedReader(new FileReader(file))) {
                Animation animation = gson.fromJson(reader, Animation.class);
                Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getChapterByIndex(animation.getChapterIndex());
                Scene scene = chapter.getSceneByName(animation.getSceneName());
                scene.setChapter(chapter);
                animation.setScene(scene);
                return animation;
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    public static List<Character> getCharactersFromDirectory() {
        List<Character> finalList = new ArrayList<>();
        File directory = NarrativeCraftFile.characterDirectory;
        File[] files = directory.listFiles();
        if(files == null) {
            NarrativeCraftMod.LOG.warn("Couldn't retrieve characters files!");
            return finalList;
        }
        Gson gson = new GsonBuilder().create();
        for(File file : files) {
            try(Reader reader = new BufferedReader(new FileReader(file))) {
                Character character = gson.fromJson(reader, Character.class);
                finalList.add(character);
            } catch (IOException e) {
                NarrativeCraftMod.LOG.warn("File {} couldn't be opened", file.getName());
            }
        }

        return finalList;
    }

    public static List<Chapter> getChaptersFromDirectory() {
        List<Chapter> finalList = new ArrayList<>();
        File directory = NarrativeCraftFile.chapterDirectory;
        File[] files = directory.listFiles();
        if(files == null) {
            NarrativeCraftMod.LOG.warn("Couldn't retrieve chapters files!");
            return finalList;
        }
        Gson gson = new GsonBuilder().create();
        for(File file : files) {
            try(Reader reader = new BufferedReader(new FileReader(file))) {
                Chapter chapter = gson.fromJson(reader, Chapter.class);
                for(Scene scene : chapter.getScenes()) {
                    scene.setChapter(chapter);
                }
                finalList.add(chapter);
            } catch (IOException e) {
                NarrativeCraftMod.LOG.warn("File {} couldn't be opened", file.getName());
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

    private static void save(Object element, File file, boolean prettyString) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try(Writer writer = new BufferedWriter(new FileWriter(file))) {
            gson.toJson(element, writer);
        }
    }

    private static File createDirectory(File parent, String name) {
        File directory = new File(parent, name);
        if(!directory.exists()) {
            if(!directory.mkdir()) NarrativeCraftMod.LOG.warn("Couldn't create directory {}!", name);
        }
        return directory;
    }

    private static File createFile(File parent, String name) {
        File file = new File(parent, name + EXTENSTION_FILE);
        if(!file.exists()) {
            try {
                if(!file.createNewFile()) NarrativeCraftMod.LOG.warn("Couldn't create file {}!", file.getAbsoluteFile());
            } catch (IOException e) {
                throw new RuntimeException("Couldn't create file: " + e);
            }
        }
        return file;
    }


}
