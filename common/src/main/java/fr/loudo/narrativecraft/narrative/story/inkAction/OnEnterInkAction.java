package fr.loudo.narrativecraft.narrative.story.inkAction;

import com.bladecoder.ink.runtime.StoryState;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.StorySave;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;

import java.util.Arrays;
import java.util.List;

public class OnEnterInkAction extends InkAction {

    public OnEnterInkAction(StoryHandler storyHandler) {
        super(storyHandler);
    }

    @Override
    public boolean execute(String[] command) {
        StoryState state = storyHandler.getStory().getState();
        String currentKnot = state.getCurrentKnot();
        if(currentKnot == null) return true;
        if(!currentKnot.equals(NarrativeCraftFile.getChapterSceneSneakCase(storyHandler.getPlayerSession().getScene()))) {
            String[] chapterSceneName = currentKnot.split("_");
            int chapterIndex = Integer.parseInt(chapterSceneName[1]);
            List<String> splitSceneName = Arrays.stream(chapterSceneName).toList().subList(2, chapterSceneName.length);
            String sceneName = String.join(" ", splitSceneName);
            Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getChapterByIndex(chapterIndex);
            Scene scene = chapter.getSceneByName(sceneName);
            storyHandler.getPlayerSession().setChapter(chapter);
            storyHandler.getPlayerSession().setScene(scene);
            storyHandler.save();
            sendDebugDetails();
        }
        return true;
    }

    @Override
    void sendDebugDetails() {
        if(storyHandler.isDebugMode()) {
            Minecraft.getInstance().player.displayClientMessage(
                    Translation.message("debug.switch_chapter_scene", storyHandler.getPlayerSession().getChapter().getIndex(), storyHandler.getPlayerSession().getScene().getName()),
                    false);
        }
    }

    @Override
    public ErrorLine validate(String[] command, int line, String lineText, Scene scene) {
        return null;
    }
}
