package fr.loudo.narrativecraft.narrative.story.inkAction;

import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;

public class SaveInkAction extends InkAction {

    public SaveInkAction(StoryHandler storyHandler) {
        super(storyHandler, InkTagType.SAVE);
    }

    @Override
    public InkActionResult execute(String[] command) {
        storyHandler.save(false);
        return InkActionResult.PASS;
    }

    @Override
    void sendDebugDetails() {

    }

    @Override
    public ErrorLine validate(String[] command, int line, String lineText, Scene scene) {
        return null;
    }
}
