package fr.loudo.narrativecraft.narrative.story;

import com.bladecoder.ink.runtime.StoryException;
import fr.loudo.narrativecraft.narrative.story.inkAction.EmoteCraftInkAction;
import fr.loudo.narrativecraft.narrative.story.inkAction.InkAction;
import fr.loudo.narrativecraft.narrative.story.inkAction.InkActionResult;
import fr.loudo.narrativecraft.narrative.story.inkAction.enums.InkTagType;
import fr.loudo.narrativecraft.platform.Services;

import java.util.ArrayList;
import java.util.List;

public class InkTagTranslators {

    private final StoryHandler storyHandler;
    private List<String> tagsToExecuteLater;

    public InkTagTranslators(StoryHandler storyHandler) {
        this.storyHandler = storyHandler;
        this.tagsToExecuteLater = new ArrayList<>();
    }

    public boolean executeCurrentTags() throws Exception {
        List<String> tags = storyHandler.getStory().getCurrentTags();
        return executeTags(tags);
    }

    public boolean executeTags(List<String> tags) throws Exception {
        for (int i = 0; i < tags.size(); i++) {
            String tag = tags.get(i);
            InkActionResult inkActionResult = executeTag(tag);
            if(inkActionResult.isError()) {
                throw new StoryException(inkActionResult.getErrorMessage());
            }
            if(inkActionResult.getStatus() == InkActionResult.Status.BLOCK) {
                tagsToExecuteLater = tags.subList(i + 1, tags.size());
                return false;
            }
        }
        if(storyHandler.isRunning()) {
            if(storyHandler.isFinished()) {
                storyHandler.stop(false);
            }
        }
        return true;
    }

    public void executeLaterTags() {
        for (int i = 0; i < tagsToExecuteLater.size(); i++) {
            String tag = tagsToExecuteLater.get(i);
            InkActionResult inkActionResult = executeTag(tag);
            if(inkActionResult.isError()) {
                storyHandler.crash(new Exception(inkActionResult.getErrorMessage()), true);
                return;
            }
            if(executeTag(tag).getStatus() == InkActionResult.Status.BLOCK) {
                tagsToExecuteLater = tagsToExecuteLater.subList(i + 1, tagsToExecuteLater.size());
                return;
            }
        }
        tagsToExecuteLater.clear();
        if(storyHandler.isRunning()) {
            storyHandler.showDialog();
            if(storyHandler.isFinished()) {
                storyHandler.stop(false);
            }
        }
    }

    public InkActionResult executeTag(String tag) {
        InkTagType tagType = InkTagType.resolveType(tag);
        if(tagType == null) {
            return InkActionResult.error(this.getClass(), "Tag \"%s\" cannot be recognized.");
        }
        try {
            InkAction inkAction = tagType.instantiate(storyHandler, tag);
            if(inkAction instanceof EmoteCraftInkAction && !Services.PLATFORM.isModLoaded("emotecraft")) return InkActionResult.pass();
            return inkAction.execute(); // If action return false, then it's a blocking command e.g. cutscene (it will wait for the cutscene to end before continuing)
        } catch (Exception e) {
            storyHandler.crash(e, false);
        }
        return InkActionResult.pass();
    }
    public List<String> getTagsToExecuteLater() {
        return tagsToExecuteLater;
    }

}
