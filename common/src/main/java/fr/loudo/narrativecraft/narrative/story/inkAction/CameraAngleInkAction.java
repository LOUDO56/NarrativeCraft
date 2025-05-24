package fr.loudo.narrativecraft.narrative.story.inkAction;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngle;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleCharacterPosition;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleGroup;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;

public class CameraAngleInkAction extends InkAction {

    private String child;

    public CameraAngleInkAction(StoryHandler storyHandler) {
        super(storyHandler);
    }

    @Override
    public boolean execute(String[] command) {
        storyHandler.setCurrentKeyframeCoordinate(null);
        name = command[2];
        child = command[3];
        CameraAngleGroup cameraAngleGroup = storyHandler.getPlayerSession().getScene().getCameraAnglesGroupByName(name);
        CameraAngle cameraAngle = cameraAngleGroup.getCameraAngleByName(child);
        if(cameraAngle != null) {
            executeCameraAngle(cameraAngleGroup, cameraAngle);
            sendDebugDetails();
            return false;
        }
        return true;
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

    @Override
    void sendDebugDetails() {
        if(storyHandler.isDebugMode()) {
            Minecraft.getInstance().player.displayClientMessage(Translation.message("debug.cutscene", name, child), false);
        }
    }
}
