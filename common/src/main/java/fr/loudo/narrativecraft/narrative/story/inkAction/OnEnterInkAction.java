package fr.loudo.narrativecraft.narrative.story.inkAction;

import com.bladecoder.ink.runtime.StoryState;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;

public class OnEnterInkAction extends InkAction {

    public OnEnterInkAction(StoryHandler storyHandler, String command) {
        super(storyHandler, InkTagType.ON_ENTER, command);
    }

    @Override
    public InkActionResult execute() {
        StoryState state = storyHandler.getStory().getState();
        String currentKnot = state.getCurrentKnot();
        if(currentKnot == null) return InkActionResult.PASS;
        if(!currentKnot.equals(NarrativeCraftFile.getChapterSceneSnakeCase(storyHandler.getPlayerSession().getScene()))) {
            for(CharacterStory characterStory : storyHandler.getCurrentCharacters()) {
                NarrativeCraftMod.server.execute(characterStory::kill);
            }
            storyHandler.getCurrentCharacters().clear();
            storyHandler.getPlayerSession().reset();
            storyHandler.initChapterSceneSession();
            storyHandler.setCurrentDialogBox(null);
            storyHandler.getInkActionList().clear();
            storyHandler.stopAllSound();
            storyHandler.save(true);
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
