package fr.loudo.narrativecraft.narrative.chapter;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.scenes.Scene;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.commands.CommandSourceStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the chapters in the narrative.
 */
public class ChapterManager {

    private List<Chapter> chapters;

    /**
     * Initializes the ChapterManager with an empty list of chapters.
     */
    public ChapterManager() {
        this.chapters = new ArrayList<>();
    }

    /**
     * Checks if a chapter exists by its index.
     *
     * @param index the index of the chapter to check.
     * @return true if the chapter exists, false otherwise.
     */
    public boolean chapterExists(int index) {
        for (Chapter chapter : chapters) {
            if (chapter.getIndex() == index) {
                return true;
            }
        }
        return false;
    }

    /**
     * Provides suggestions for chapters to be used in a command context.
     *
     * @return a SuggestionProvider for chapters.
     */
    public SuggestionProvider<CommandSourceStack> getChapterSuggestions() {
        return (context, builder) -> {
            for (Chapter chapter : chapters) {
                builder.suggest(chapter.getIndex());
            }
            return builder.buildFuture();
        };
    }

    /**
     * Adds a new chapter to the list of chapters if it does not already exist.
     *
     * @param newChapter the new chapter to add.
     * @return true if the chapter was added, false if it already exists.
     */
    public boolean addChapter(Chapter newChapter) {
        if (chapters.contains(newChapter)) return false;
        try {
            chapters.add(newChapter);
            NarrativeCraftFile.saveChapter(newChapter);
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.warn("Couldn't save chapter " + newChapter.getIndex() + " file: " + e);
            return false;
        }
    }

    public boolean removeChapter(Chapter chapter) {
        if (!chapters.contains(chapter)) return false;
        try {
            chapters.remove(chapter);
            NarrativeCraftFile.removeChapter(chapter);
            return true;
        } catch (IOException e) {
            NarrativeCraftMod.LOG.warn("Couldn't remove chapter " + chapter.getIndex() + " file: " + e);
            return false;
        }
    }

    public SuggestionProvider<CommandSourceStack> getSceneSuggestionsByChapter() {
        return (context, builder) -> {
            int chapterIndex = IntegerArgumentType.getInteger(context, "chapter_index");
            Chapter chapter = getChapterByIndex(chapterIndex);
            for (Scene scene : chapter.getScenes()) {
                builder.suggest(scene.getName());
            }
            return builder.buildFuture();
        };
    }

    public SuggestionProvider<CommandSourceStack> getAnimationSuggestionByScene() {
        return (context, builder) -> {
            int chapterIndex = IntegerArgumentType.getInteger(context, "chapter_index");
            String sceneName = StringArgumentType.getString(context, "scene_name");
            Chapter chapter = getChapterByIndex(chapterIndex);
            Scene scene = chapter.getSceneByName(sceneName);
            for (String animationName : scene.getAnimationFilesName()) {
                builder.suggest(animationName);
            }
            return builder.buildFuture();
        };
    }

    public SuggestionProvider<CommandSourceStack> getAnimationSuggestionBySceneFromSession() {
        return (context, builder) -> {
            PlayerSession playerSession = Utils.getSessionOrNull(context.getSource().getPlayer());
            if(playerSession == null) return builder.buildFuture();
            Scene scene = playerSession.getScene();
            for (String animationName : scene.getAnimationFilesName()) {
                String finalAnimationName = animationName.split("\\.")[2];
                builder.suggest(finalAnimationName);
            }
            return builder.buildFuture();
        };
    }

    public SuggestionProvider<CommandSourceStack> getCutsceneSuggestionBySceneFromSession() {
        return (context, builder) -> {
            PlayerSession playerSession = Utils.getSessionOrNull(context.getSource().getPlayer());
            if(playerSession == null) return builder.buildFuture();
            Scene scene = playerSession.getScene();
            for (String cutsceneName : scene.getCutsceneFilesName()) {
                String finalAnimationName = cutsceneName.split("\\.")[2];
                builder.suggest(finalAnimationName);
            }
            return builder.buildFuture();
        };
    }

    public SuggestionProvider<CommandSourceStack> getSubsceneSuggestionByScene() {
        return (context, builder) -> {
            PlayerSession playerSession = Utils.getSessionOrNull(context.getSource().getPlayer());
            if(playerSession == null) return builder.buildFuture();
            for (Subscene subscene : playerSession.getScene().getSubsceneList()) {
                builder.suggest(subscene.getName());
            }
            return builder.buildFuture();
        };
    }

    /**
     * Retrieves a chapter by its index.
     *
     * @param indexChapter the index of the chapter to retrieve.
     * @return the Chapter object if found, null otherwise.
     */
    public Chapter getChapterByIndex(int indexChapter) {
        for (Chapter chapter : chapters) {
            if (chapter.getIndex() == indexChapter) {
                return chapter;
            }
        }
        return null;
    }

    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }

}

