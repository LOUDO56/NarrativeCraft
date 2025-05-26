package fr.loudo.narrativecraft.narrative.story.inkAction;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.KeyframeControllerBase;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.Cutscene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutscenePlayback;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;

public class CutsceneInkAction extends InkAction {
    public CutsceneInkAction(StoryHandler storyHandler) {
        super(storyHandler);
    }

    @Override
    public boolean execute(String[] command) {
        storyHandler.setCurrentKeyframeCoordinate(null);
        name = command[2];
        Cutscene cutscene = storyHandler.getPlayerSession().getScene().getCutsceneByName(name);
        if(cutscene != null) {
            executeCutscene(cutscene);
            return false;
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
        sendDebugDetails();
    }

    private void handleEndCutscene(CutsceneController cutsceneController) {
        for(Playback playback : cutsceneController.getPlaybackList()) {
            NarrativeCraftMod.getInstance().getPlaybackHandler().getPlaybacks().remove(playback);
        }
        storyHandler.setCurrentKeyframeCoordinate(cutsceneController.getCutscene().getKeyframeGroupList().getLast().getKeyframeList().getLast().getKeyframeCoordinate());
        storyHandler.getInkTagTranslators().executeLaterTags();
        if(storyHandler.getInkTagTranslators().getTagsToExecuteLater().isEmpty() && storyHandler.isFinished()) {
            storyHandler.stop();
        }

    }

    @Override
    void sendDebugDetails() {
        if(storyHandler.isDebugMode()) {
            Minecraft.getInstance().player.displayClientMessage(Translation.message("debug.cutscene", name), false);
        }
    }
}
