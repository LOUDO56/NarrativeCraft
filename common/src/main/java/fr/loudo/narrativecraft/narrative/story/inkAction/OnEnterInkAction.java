package fr.loudo.narrativecraft.narrative.story.inkAction;

import com.bladecoder.ink.runtime.StoryState;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;

import java.util.Arrays;
import java.util.List;

public class OnEnterInkAction extends InkAction {

    public OnEnterInkAction(StoryHandler storyHandler) {
        super(storyHandler);
    }

    @Override
    public InkActionResult execute(String[] command) {
        StoryState state = storyHandler.getStory().getState();
        String currentKnot = state.getCurrentKnot();
        if(currentKnot == null) return InkActionResult.PASS;
        if(!currentKnot.equals(NarrativeCraftFile.getChapterSceneSneakCase(storyHandler.getPlayerSession().getScene()))) {
            storyHandler.initChapterSceneSession();
            storyHandler.save();
            sendDebugDetails();
        }
        return InkActionResult.PASS;
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
