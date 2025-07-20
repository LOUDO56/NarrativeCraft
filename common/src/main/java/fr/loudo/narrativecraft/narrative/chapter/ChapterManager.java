package fr.loudo.narrativecraft.narrative.chapter;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import net.minecraft.commands.CommandSourceStack;

import java.util.List;

/**
 * Manages the chapters in the narrative.
 */
public class ChapterManager {

    private List<Chapter> chapters;

    public void init() {
        chapters = ChaptersInit.init();
    }
    public List<Chapter> getChapters() {
        return chapters;
    }

    public boolean addChapter(String name, String description) {
        Chapter chapter = new Chapter(chapters.size() + 1, name, description);
        if(NarrativeCraftFile.createChapterDirectory(chapter)) {
            chapters.add(chapter);
            return true;
        } else {
            return false;
        }
    }

    public SuggestionProvider<CommandSourceStack> getChapterSuggestions() {
        return (context, builder) -> {
            if(chapters == null) return builder.buildFuture();
            for (Chapter chapter : chapters) {
                builder.suggest(chapter.getIndex());
            }
            return builder.buildFuture();
        };
    }

    public boolean chapterExists(int chapterIndex) {
        for(Chapter chapter : chapters) {
            if(chapter.getIndex() == chapterIndex){
                return true;
            }
        }
        return false;
    }

    public Chapter getChapterByIndex(int chapterIndex) {
        for(Chapter chapter : chapters) {
            if(chapter.getIndex() == chapterIndex){
                return chapter;
            }
        }
        return null;
    }

    public void removeChapter(Chapter chapter) {
        chapters.remove(chapter);
    }

    public SuggestionProvider<CommandSourceStack> getSceneSuggestionsByChapter() {
        return (context, builder) -> {
            int chapterIndex = IntegerArgumentType.getInteger(context, "chapter_index");
            Chapter chapter = getChapterByIndex(chapterIndex);
            if(chapter == null) return builder.buildFuture();
            for (Scene scene : chapter.getSceneList()) {
                if(scene.getName().split(" ").length > 1) {
                    builder.suggest("\"" + scene.getName() + "\"");
                } else {
                    builder.suggest(scene.getName());
                }
            }
            return builder.buildFuture();
        };
    }

    public SuggestionProvider<CommandSourceStack> getSubscenesOfScenesSuggestions() {
        return (context, builder) -> {
            PlayerSession playerSession = NarrativeCraftMod.getInstance().getPlayerSession();
            if(!playerSession.sessionSet()) return builder.buildFuture();
            for (Subscene subscene : playerSession.getScene().getSubsceneList()) {
                if(subscene.getName().split(" ").length > 1) {
                    builder.suggest("\"" + subscene.getName() + "\"");
                } else {
                    builder.suggest(subscene.getName());
                }
            }
            return builder.buildFuture();
        };
    }
}

