package fr.loudo.narrativecraft.narrative.story;

import fr.loudo.narrativecraft.narrative.story.inkAction.InkAction;
import fr.loudo.narrativecraft.narrative.story.inkAction.enums.InkActionResult;
import fr.loudo.narrativecraft.narrative.story.inkAction.enums.InkTagType;

import java.util.ArrayList;
import java.util.List;

public class InkTagTranslators {

    private final StoryHandler storyHandler;
    private List<String> tagsToExecuteLater;

    public InkTagTranslators(StoryHandler storyHandler) {
        this.storyHandler = storyHandler;
        this.tagsToExecuteLater = new ArrayList<>();
    }

    public boolean executeCurrentTags() {
        try {
            List<String> tags = storyHandler.getStory().getCurrentTags();
            return executeTags(tags);
        } catch (Exception e) {
            storyHandler.crash(e, false);
            return false;
        }
    }

    public boolean executeTags(List<String> tags) {
        try {
            for (int i = 0; i < tags.size(); i++) {
                String tag = tags.get(i);
                if(executeTag(tag) == InkActionResult.BLOCK) {
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
        } catch (Exception e) {
            storyHandler.crash(e, false);
            return false;
        }
    }

    public void executeLaterTags() {
        for (int i = 0; i < tagsToExecuteLater.size(); i++) {
            String tag = tagsToExecuteLater.get(i);
            if(executeTag(tag) == InkActionResult.BLOCK) {
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
            storyHandler.crash(new Exception(String.format("Tag \"%s\" cannot be recognized.", tag)), true);
            return InkActionResult.ERROR;
        }
        try {
            InkAction inkAction = tagType.instantiate(storyHandler, tag);
            return inkAction.execute(); // If action return false, then it's a blocking command e.g. cutscene (it will wait for the cutscene to end before continuing)
        } catch (Exception e) {
            storyHandler.crash(e, false);
        }
        return InkActionResult.PASS;
    }
    public List<String> getTagsToExecuteLater() {
        return tagsToExecuteLater;
    }

}
