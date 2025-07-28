package fr.loudo.narrativecraft.narrative.story.inkAction;

import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngle;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleGroup;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.inkAction.InkActionResult;
import fr.loudo.narrativecraft.narrative.story.inkAction.enums.InkTagType;
import fr.loudo.narrativecraft.narrative.story.inkAction.validation.ErrorLine;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;

public class CameraAngleInkAction extends InkAction {

    private String child;

    public CameraAngleInkAction(StoryHandler storyHandler, String command) {
        super(storyHandler, InkTagType.CAMERA_ANGLE, command);
    }

    @Override
    public InkActionResult execute() {
        if(command.length < 3) {
            return InkActionResult.error(this.getClass(), Translation.message("validation.camera_angle_missing_parent").getString());
        }
        storyHandler.getPlayerSession().setSoloCam(null);
        name = InkAction.parseName(command, 2);
        child = InkAction.parseName(command, command.length - 1);
        CameraAngleGroup cameraAngleGroup = storyHandler.getPlayerSession().getScene().getCameraAnglesGroupByName(name);
        if(cameraAngleGroup == null) {
            return InkActionResult.error(this.getClass(), Translation.message("validation.camera_angle_parent", name).getString());
        }
        CameraAngle cameraAngle = cameraAngleGroup.getCameraAngleByName(child);
        if(cameraAngle == null) {
            return InkActionResult.error(this.getClass(), Translation.message("validation.camera_angle_child", child, name).getString());
        }
        executeCameraAngle(cameraAngleGroup, cameraAngle);
        sendDebugDetails();
        return InkActionResult.pass();
    }

    private void executeCameraAngle(CameraAngleGroup cameraAngleGroup, CameraAngle cameraAngle) {
        CameraAngleController cameraAngleController = (CameraAngleController) storyHandler.getPlayerSession().getKeyframeControllerBase();
        if(cameraAngleController == null) {
            cameraAngleController = new CameraAngleController(cameraAngleGroup, Utils.getServerPlayerByUUID(Minecraft.getInstance().player.getUUID()), Playback.PlaybackType.PRODUCTION);
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
                    lineText, false
            );
        }
        if(command.length < 4) {
            return new ErrorLine(
                    line,
                    scene,
                    Translation.message("validation.camera_angle_missing_child").getString(),
                    lineText, false
            );
        }
        String parent = InkAction.parseName(command, 2);
        String child = InkAction.parseName(command, command.length - 1);
        CameraAngleGroup cameraAngleGroup = scene.getCameraAnglesGroupByName(parent);
        if(cameraAngleGroup == null) {
            return new ErrorLine(
                    line,
                    scene,
                    Translation.message("validation.camera_angle_parent", parent).getString(),
                    lineText, false
            );
        }
        CameraAngle cameraAngle = cameraAngleGroup.getCameraAngleByName(child);
        if(cameraAngle == null) {
            return new ErrorLine(
                    line,
                    scene,
                    Translation.message("validation.camera_angle_child", child, parent).getString(),
                    lineText, false
            );
        }
        return null;
    }


}
