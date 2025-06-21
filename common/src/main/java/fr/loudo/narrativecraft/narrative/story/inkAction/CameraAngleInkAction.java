package fr.loudo.narrativecraft.narrative.story.inkAction;

import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngle;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleGroup;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;

public class CameraAngleInkAction extends InkAction {

    private String child;

    public CameraAngleInkAction() {}

    public CameraAngleInkAction(StoryHandler storyHandler) {
        super(storyHandler);
    }

    @Override
    public InkActionResult execute(String[] command) {
        storyHandler.getPlayerSession().setSoloCam(null);
        name = command[2];
        child = command[3];
        CameraAngleGroup cameraAngleGroup = storyHandler.getPlayerSession().getScene().getCameraAnglesGroupByName(name);
        CameraAngle cameraAngle = cameraAngleGroup.getCameraAngleByName(child);
        if(cameraAngle != null) {
            executeCameraAngle(cameraAngleGroup, cameraAngle);
            sendDebugDetails();
        }
        return InkActionResult.PASS;
    }

    private void executeCameraAngle(CameraAngleGroup cameraAngleGroup, CameraAngle cameraAngle) {
        CameraAngleController cameraAngleController = (CameraAngleController) storyHandler.getPlayerSession().getKeyframeControllerBase();
        if(cameraAngleController == null) {
            cameraAngleController = new CameraAngleController(cameraAngleGroup, storyHandler.getPlayerSession().getPlayer(), Playback.PlaybackType.PRODUCTION);
            cameraAngleController.startSession();
            storyHandler.getPlayerSession().setKeyframeControllerBase(cameraAngleController);
        }
        cameraAngleController.setCurrentPreviewKeyframe(cameraAngle);
    }

    @Override
    void sendDebugDetails() {
        if(storyHandler.isDebugMode()) {
            Minecraft.getInstance().player.displayClientMessage(Translation.message("debug.camera_angle", name, child), false);
        }
    }

    @Override
    public ErrorLine validate(String[] command, int line, String lineText, Scene scene) {
        if(command.length < 3) {
            return new ErrorLine(
                    line,
                    scene,
                    Translation.message("validation.camera_angle_missing_parent").getString(),
                    lineText
            );
        }
        if(command.length < 4) {
            return new ErrorLine(
                    line,
                    scene,
                    Translation.message("validation.camera_angle_missing_child").getString(),
                    lineText
            );
        }
        String parent = command[2];
        String child = command[3];
        CameraAngleGroup cameraAngleGroup = scene.getCameraAnglesGroupByName(parent);
        if(cameraAngleGroup == null) {
            return new ErrorLine(
                    line,
                    scene,
                    Translation.message("validation.camera_angle_parent", parent).getString(),
                    lineText
            );
        }
        CameraAngle cameraAngle = cameraAngleGroup.getCameraAngleByName(child);
        if(cameraAngle == null) {
            return new ErrorLine(
                    line,
                    scene,
                    Translation.message("validation.camera_angle_child", child, parent).getString(),
                    lineText
            );
        }
        return null;
    }


}
