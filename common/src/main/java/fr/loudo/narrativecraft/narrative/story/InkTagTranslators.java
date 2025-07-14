package fr.loudo.narrativecraft.narrative.story;

import fr.loudo.narrativecraft.narrative.story.inkAction.*;
import fr.loudo.narrativecraft.screens.credits.CreditsScreen;
import net.minecraft.client.Minecraft;

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
            throw new RuntimeException(e);
        }
    }

    public boolean executeTags(List<String> tags) {
        try {
            for (int i = 0; i < tags.size(); i++) {
                String tag = tags.get(i);
                if(executeTag(tag) == InkAction.InkActionResult.BLOCK) {
                    tagsToExecuteLater = tags.subList(i + 1, tags.size());
                    return false;
                }
            }
            if(storyHandler.getStory() != null) {
                if(storyHandler.isFinished()) {
                    storyHandler.stop(false);
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
            if(executeTag(tag) == InkAction.InkActionResult.BLOCK) {
                tagsToExecuteLater = tagsToExecuteLater.subList(i + 1, tagsToExecuteLater.size());
                return;
            }
        }
        tagsToExecuteLater.clear();
        if(storyHandler.getStory() != null) {
            storyHandler.showDialog();
            if(storyHandler.isFinished()) {
                storyHandler.stop(false);
            }
        }

    }

    public InkAction.InkActionResult executeTag(String tag) {
        InkAction inkAction = null;
        InkAction.InkTagType tagType = InkAction.getInkActionTypeByTag(tag);
        switch (tagType) {
            case ON_ENTER -> inkAction = new OnEnterInkAction(storyHandler, tag);
            case CUTSCENE -> inkAction = new CutsceneInkAction(storyHandler, tag);
            case CAMERA_ANGLE ->  inkAction = new CameraAngleInkAction(storyHandler, tag);
            case SONG_SFX_START, SONG_SFX_STOP -> inkAction = new SongSfxInkAction(storyHandler, tag);
            case SOUND_STOP_ALL -> storyHandler.stopAllSound();
            case FADE -> inkAction = new FadeScreenInkAction(storyHandler, tag);
            case WAIT -> inkAction = new WaitInkAction(storyHandler, tag);
            case SAVE -> inkAction = new SaveInkAction(storyHandler, tag);
            case SUBSCENE -> inkAction = new SubscenePlayInkAction(storyHandler, tag);
            case ANIMATION -> inkAction = new AnimationPlayInkAction(storyHandler, tag);
            case DAYTIME -> inkAction = new ChangeDayTimeInkAction(storyHandler, tag);
            case WEATHER -> inkAction = new WeatherChangeInkAction(storyHandler, tag);
            case MINECRAFT_COMMAND -> inkAction = new CommandMinecraftInkAction(storyHandler, tag);
            case DIALOG_VALUES -> inkAction = new DialogValuesInkAction(storyHandler, tag);
            case SHAKE -> inkAction = new ShakeScreenInkAction(storyHandler, tag);
            case EMOTE -> inkAction = new EmoteCraftInkAction(storyHandler, tag);
            case KILL_CHARACTER -> inkAction = new KillCharacterInkAction(storyHandler, tag);
            case BORDER -> inkAction = new BorderInkAction(storyHandler, tag);
            case null -> {}
        }
        if(inkAction == null) return InkAction.InkActionResult.PASS; // If there's no action, then continue story
        return inkAction.execute(); // If action return false, then it's a blocking command e.g. cutscene (it will wait for the cutscene to end before continuing)
    }
    public List<String> getTagsToExecuteLater() {
        return tagsToExecuteLater;
    }

}
