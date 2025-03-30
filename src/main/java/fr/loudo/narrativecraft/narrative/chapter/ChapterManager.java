package fr.loudo.narrativecraft.narrative.chapter;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
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
    public boolean addChapter(Chapter newChapter) throws IOException {
        if (chapters.contains(newChapter)) return false;
        NarrativeCraftFile.saveChapter(newChapter);
        chapters.add(newChapter);
        return true;
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

}

