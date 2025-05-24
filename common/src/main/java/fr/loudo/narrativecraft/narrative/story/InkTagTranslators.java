package fr.loudo.narrativecraft.narrative.story;

import fr.loudo.narrativecraft.narrative.story.inkAction.CameraAngleInkAction;
import fr.loudo.narrativecraft.narrative.story.inkAction.CutsceneInkAction;
import fr.loudo.narrativecraft.narrative.story.inkAction.InkAction;
import fr.loudo.narrativecraft.narrative.story.inkAction.SongSfxInkAction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.ArrayList;
import java.util.List;

public class InkTagTranslators {

    private final StoryHandler storyHandler;
    private final List<String> tagsToExecuteLater;

    public InkTagTranslators(StoryHandler storyHandler) {
        this.storyHandler = storyHandler;
        this.tagsToExecuteLater = new ArrayList<>();
    }

    public boolean executeCurrentTags() {
        try {
            List<String> tags = storyHandler.getStory().getCurrentTags();
            for (int i = 0; i < tags.size(); i++) {
                String tag = tags.get(i);
                if(!executeTag(tag)) {
                    tagsToExecuteLater.addAll(tags.subList(i + 1, tags.size()));
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void executeLaterTags() {
        for (int i = 0; i < tagsToExecuteLater.size(); i++) {
            String tag = tagsToExecuteLater.get(i);
            if(!executeTag(tag)) {
                tagsToExecuteLater.addAll(tagsToExecuteLater.subList(i + 1, tagsToExecuteLater.size() - 1));
                break;
            }
        }
        storyHandler.showDialog();

    }

    public boolean executeTag(String tag) {
        String[] tagSplit = tag.split(" ");
        InkAction inkAction = null;
        if(tag.contains("cutscene start")) {
            inkAction = new CutsceneInkAction(storyHandler);
        } else if(tag.contains("camera set")) {
            inkAction = new CameraAngleInkAction(storyHandler);
        } else if (tag.contains("song start") || tag.contains("sfx start")) {
            SongSfxInkAction.SoundType soundType;
            if(tag.contains("song start")) soundType = SongSfxInkAction.SoundType.SONG;
            else soundType = SongSfxInkAction.SoundType.SFX;
            inkAction = new SongSfxInkAction(storyHandler, soundType);
        } else if (tag.contains("song stop") || tag.contains("sfx stop")) {
            if(tagSplit.length < 3) {
                throw new RuntimeException("No parameter set for song/sfx");
            }
            String value = tagSplit[2];
            if(value.equalsIgnoreCase("all")) {
                SongSfxInkAction.SoundType soundType;
                if(tag.contains("song start")) soundType = SongSfxInkAction.SoundType.SONG;
                else soundType = SongSfxInkAction.SoundType.SFX;
                storyHandler.stopAllSoundByType(soundType);
            } else {
                ResourceLocation soundRes = ResourceLocation.withDefaultNamespace(value);
                SoundEvent sound = SoundEvent.createVariableRangeEvent(soundRes);
                storyHandler.stopSound(sound);
            }
        } else if (tag.contains("sound stop all")) {
            storyHandler.stopAllSound();
        }
        if(inkAction == null) return true; // If there's no action, then continue story
        return inkAction.execute(tagSplit); // If action return false, then it's a blocking command e.g. cutscene (it will wait for the cutscene to end before continuing)
    }
    public List<String> getTagsToExecuteLater() {
        return tagsToExecuteLater;
    }
}
