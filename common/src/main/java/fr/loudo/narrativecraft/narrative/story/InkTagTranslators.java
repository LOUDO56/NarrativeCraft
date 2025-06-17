package fr.loudo.narrativecraft.narrative.story;

import fr.loudo.narrativecraft.narrative.story.inkAction.*;

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
            for (int i = 0; i < tags.size(); i++) {
                String tag = tags.get(i);
                if(executeTag(tag) == InkAction.InkActionResult.BLOCK) {
                    tagsToExecuteLater = tags.subList(i + 1, tags.size());
                    return false;
                }
            }
            if(storyHandler.isFinished()) {
                storyHandler.stop();
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void executeLaterTags() {
        for (int i = 0; i < tagsToExecuteLater.size(); i++) {
            String tag = tagsToExecuteLater.get(i);
            if(executeTag(tag) == InkAction.InkActionResult.BLOCK) {
                tagsToExecuteLater = tagsToExecuteLater.subList(i + 1, tagsToExecuteLater.size());
                return;
            }
        }
        tagsToExecuteLater.clear();
        storyHandler.showDialog();
        if(storyHandler.isFinished()) {
            storyHandler.stop();
        }

    }

    public InkAction.InkActionResult executeTag(String tag) {
        String[] tagSplit = tag.split(" ");
        InkAction inkAction = null;
        InkAction.InkTagType tagType = InkAction.getInkActionTypeByTag(tag);
        switch (tagType) {
            case ON_ENTER -> inkAction = new OnEnterInkAction(storyHandler);
            case CUTSCENE -> inkAction = new CutsceneInkAction(storyHandler);
            case CAMERA_ANGLE ->  inkAction = new CameraAngleInkAction(storyHandler);
            case SONG_SFX_START -> {
                SongSfxInkAction.SoundType soundType;
                if(tag.contains("song start")) soundType = SongSfxInkAction.SoundType.SONG;
                else soundType = SongSfxInkAction.SoundType.SFX;
                inkAction = new SongSfxInkAction(storyHandler, soundType);
            }
            case SONG_SFX_STOP -> {
                if(tagSplit.length < 3) {
                    throw new RuntimeException("No parameter set for song/sfx");
                }
                String value = tagSplit[2];
                if(value.equalsIgnoreCase("all")) {
                    SongSfxInkAction.SoundType soundType;
                    if(tag.contains("song stop")) soundType = SongSfxInkAction.SoundType.SONG;
                    else soundType = SongSfxInkAction.SoundType.SFX;
                    storyHandler.stopAllSoundByType(soundType);
                } else {
                    for(InkAction inkAction1 : storyHandler.getInkActionList()) {
                        if(inkAction1.getName().equals(value)) {
                            inkAction = inkAction1;
                        }
                    }
                }
            }
            case SOUND_STOP_ALL -> storyHandler.stopAllSound();
            case FADE -> inkAction = new FadeScreenInkAction(storyHandler);
            case WAIT -> inkAction = new WaitInkAction(storyHandler);
            case SAVE -> inkAction = new SaveInkAction(storyHandler);
            case SUBSCENE -> inkAction = new SubscenePlayInkAction(storyHandler);
            case ANIMATION -> inkAction = new AnimationPlayInkAction(storyHandler);
            case DAYTIME -> inkAction = new ChangeDayTimeInkAction(storyHandler);
            case null -> {}
        }
        if(inkAction == null) return InkAction.InkActionResult.PASS; // If there's no action, then continue story
        return inkAction.execute(tagSplit); // If action return false, then it's a blocking command e.g. cutscene (it will wait for the cutscene to end before continuing)
    }
    public List<String> getTagsToExecuteLater() {
        return tagsToExecuteLater;
    }

}
