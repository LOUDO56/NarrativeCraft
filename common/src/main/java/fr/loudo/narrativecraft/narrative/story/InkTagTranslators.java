package fr.loudo.narrativecraft.narrative.story;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.KeyframeControllerBase;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngle;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleCharacterPosition;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleGroup;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.Cutscene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutscenePlayback;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;

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

    public void handleEndCutscene(CutsceneController cutsceneController) {
        for(Playback playback : cutsceneController.getPlaybackList()) {
            NarrativeCraftMod.getInstance().getPlaybackHandler().getPlaybacks().remove(playback);
        }
        if(tagsToExecuteLater.isEmpty()) {
            storyHandler.setCurrentKeyframeCoordinate(cutsceneController.getCutscene().getKeyframeGroupList().getLast().getKeyframeList().getLast().getKeyframeCoordinate());
            storyHandler.showDialog();
            return;
        }
        executeLaterTags();

    }

    public void executeLaterTags() {
        for (int i = 0; i < tagsToExecuteLater.size(); i++) {
            String tag = tagsToExecuteLater.get(i);
            if(!executeTag(tag)) {
                tagsToExecuteLater.addAll(tagsToExecuteLater.subList(i + 1, tagsToExecuteLater.size() - 1));
                break;
            }
            storyHandler.showDialog();
        }

    }

    public boolean executeTag(String tag) {
        if(tag.contains("start cutscene")) {
            storyHandler.setCurrentKeyframeCoordinate(null);
            String cutsceneName = tag.split(" ")[2];
            Cutscene cutscene = storyHandler.getPlayerSession().getScene().getCutsceneByName(cutsceneName);
            if(cutscene != null) {
                executeCutscene(cutscene);
                return false;
            }
        } else if(tag.contains("set camera")) {
            storyHandler.setCurrentKeyframeCoordinate(null);
            String cameraAnglesGroupName = tag.split(" ")[2];
            String cameraAngleName = tag.split(" ")[3];
            CameraAngleGroup cameraAngleGroup = storyHandler.getPlayerSession().getScene().getCameraAnglesGroupByName(cameraAnglesGroupName);
            //TODO: error handler for tags
            CameraAngle cameraAngle = cameraAngleGroup.getCameraAngleByName(cameraAngleName);
            if(cameraAngle != null) {
                executeCameraAngle(cameraAngleGroup, cameraAngle);
            }
        }
        return true;
    }

    private void executeCutscene(Cutscene cutscene) {
        for(CharacterStory characterStory : storyHandler.getCurrentCharacters()) {
            characterStory.kill();
        }
        storyHandler.getCurrentCharacters().clear();
        storyHandler.setCurrentDialogBox(null);
        KeyframeControllerBase keyframeControllerBase = storyHandler.getPlayerSession().getKeyframeControllerBase();
        if(keyframeControllerBase instanceof CameraAngleController cameraAngleController) {
            //TODO: fix concurent shit
            cameraAngleController.stopSession();
            storyHandler.getCurrentDialogBox().reset();
        }
        CutsceneController cutsceneController = new CutsceneController(cutscene, storyHandler.getPlayerSession().getPlayer(), Playback.PlaybackType.PRODUCTION);
        cutsceneController.startSession();
        storyHandler.getPlayerSession().setKeyframeControllerBase(cutsceneController);
        CutscenePlayback cutscenePlayback = new CutscenePlayback(storyHandler.getPlayerSession().getPlayer(), cutscene.getKeyframeGroupList(), cutscene.getKeyframeGroupList().getFirst().getKeyframeList().getFirst(), cutsceneController);
        cutscenePlayback.start();
        cutscenePlayback.setOnCutsceneEnd(() -> handleEndCutscene(cutsceneController));
        for(Playback playback : NarrativeCraftMod.getInstance().getPlaybackHandler().getPlaybacks()) {
            storyHandler.getCurrentCharacters().add(playback.getCharacter());
        }
    }

    private void executeCameraAngle(CameraAngleGroup cameraAngleGroup, CameraAngle cameraAngle) {
        CameraAngleController cameraAngleController = (CameraAngleController) storyHandler.getPlayerSession().getKeyframeControllerBase();
        if(cameraAngleController == null) {
            cameraAngleController = new CameraAngleController(cameraAngleGroup, storyHandler.getPlayerSession().getPlayer(), Playback.PlaybackType.PRODUCTION);
            cameraAngleController.startSession();
            storyHandler.getPlayerSession().setKeyframeControllerBase(cameraAngleController);
            storyHandler.getCurrentCharacters().clear();
            for(CameraAngleCharacterPosition characterPosition : cameraAngleController.getCameraAngleGroup().getCharacterPositions()) {
                storyHandler.getCurrentCharacters().add(characterPosition.getCharacter());
            }
        }
        cameraAngleController.setCurrentPreviewKeyframe(cameraAngle);
    }

    public List<String> getTagsToExecuteLater() {
        return tagsToExecuteLater;
    }
}
