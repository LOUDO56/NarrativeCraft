package fr.loudo.narrativecraft.files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.character.Character;
import fr.loudo.narrativecraft.narrative.cutscenes.Cutscene;
import fr.loudo.narrativecraft.narrative.recordings.actions.Action;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionDeserializer;
import fr.loudo.narrativecraft.narrative.scenes.Scene;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
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
    private static final String CUTSCENE_DIRECTORY_NAME = "cutscenes";
    private static final String EXTENSTION_FILE = ".json";

    public static File mainDirectory;
    public static File chapterDirectory;
    public static File animationDirectory;
    public static File cutsceneDirectory;
    public static File characterDirectory;

    public static void init(MinecraftServer server) {
        mainDirectory = createDirectory(server.getWorldPath(LevelResource.ROOT).toFile(), DIRECTORY_NAME);
        chapterDirectory = createDirectory(mainDirectory, CHAPTER_DIRECTORY_NAME);
        animationDirectory = createDirectory(mainDirectory, ANIMATION_DIRECTORY_NAME);
        cutsceneDirectory = createDirectory(mainDirectory, CUTSCENE_DIRECTORY_NAME);
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
            removeCutsceneFileByScene(scene);
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
        String fileName = getFileNameAnimation(animation);
        File file = createFile(animationDirectory, fileName);
        animation.getScene().addAnimation(fileName);
        saveChapter(animation.getScene().getChapter());
        save(animation, file);
    }

    public static void removeCutsceneFileByScene(Scene scene) throws IOException {
        for(String cutsceneFileName : scene.getCutsceneFilesName()) {
            removeCutsceneFile(cutsceneFileName);
        }
    }

    public static boolean removeCutsceneFile(String cutsceneName) throws IOException {
        File fileAnim = new File(cutsceneDirectory, cutsceneName + EXTENSTION_FILE);
        return fileAnim.delete();
    }

    public static void saveCutscene(Cutscene cutscene) throws IOException {
        String fileName = getCutsceneFileName(cutscene);
        File file = createFile(cutsceneDirectory, fileName);
        save(cutscene, file);
    }

    public static void saveCharacter(Character character) throws IOException {
        File file = createFile(characterDirectory, character.getName().toLowerCase());
        save(character, file, true);
    }

    public static boolean animationFileExists(int chapterIndex, String sceneName, String animationName) {
        return new File(animationDirectory, getFileNameTemplate(chapterIndex, sceneName, animationName) + EXTENSTION_FILE).exists();
    }

    public static boolean cutsceneFileExists(int chapterIndex, String sceneName, String cutsceneName) {
        return new File(cutsceneDirectory, getFileNameTemplate(chapterIndex, sceneName, cutsceneName) + EXTENSTION_FILE).exists();
    }

    public static String getFileNameAnimation(Animation animation) {
        return "ch" + animation.getScene().getChapter().getIndex() + "." + animation.getScene().getName()  + "." + animation.getName();
    }

    public static String getFileNameTemplate(int chapterIndex, String sceneName, String name) {
        return "ch" + chapterIndex + "." + sceneName  + "." + name.toLowerCase();
    }

    public static String getCutsceneFileName(Cutscene cutscene) {
        return "ch" + cutscene.getScene().getChapter().getIndex() + "." + cutscene.getScene().getName()  + "." + cutscene.getName();
    }

    public static Animation getAnimationFromFile(int chapterIndex, String sceneName, String animationName) {
        File file = new File(animationDirectory, getFileNameTemplate(chapterIndex, sceneName, animationName) + EXTENSTION_FILE);
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
                NarrativeCraftMod.LOG.warn("File {} couldn't be opened", file.getName());
            }
        }
        return null;
    }

    public static Cutscene getCutsceneFromFile(int chapterIndex, String sceneName, String cutsceneName) {
        File file = new File(cutsceneDirectory, getFileNameTemplate(chapterIndex, sceneName, cutsceneName) + EXTENSTION_FILE);
        if(file.exists()) {
            Gson gson = new GsonBuilder().registerTypeAdapter(Action.class, new ActionDeserializer()).create();
            try(Reader reader = new BufferedReader(new FileReader(file))) {
                Cutscene cutscene = gson.fromJson(reader, Cutscene.class);
                Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getChapterByIndex(cutscene.getChapterIndex());
                Scene scene = chapter.getSceneByName(cutscene.getSceneName());
                scene.setChapter(chapter);
                cutscene.setScene(scene);
                for(Subscene subscene : cutscene.getSubsceneList()) {
                    subscene.setScene(scene);
                }
                return cutscene;
            } catch (IOException e) {
                NarrativeCraftMod.LOG.warn("File {} couldn't be opened", file.getName());
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
                    for(Subscene subscene : scene.getSubsceneList()) {
                        subscene.setScene(scene);
                    }
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
