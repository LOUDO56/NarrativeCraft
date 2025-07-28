package fr.loudo.narrativecraft.narrative.story.inkAction;

import fr.loudo.narrativecraft.narrative.chapter.scenes.KeyframeControllerBase;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.Cutscene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutscenePlayback;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.inkAction.InkActionResult;
import fr.loudo.narrativecraft.narrative.story.inkAction.enums.InkTagType;
import fr.loudo.narrativecraft.narrative.story.inkAction.validation.ErrorLine;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;

public class CutsceneInkAction extends InkAction {

    public CutsceneInkAction(StoryHandler storyHandler, String command) {
        super(storyHandler, InkTagType.CUTSCENE, command);
    }

    @Override
    public InkActionResult execute() {
        if(command.length < 3) {
            return InkActionResult.error(this.getClass(), Translation.message("validation.missing_name").getString());
        }
        storyHandler.getPlayerSession().setSoloCam(null);
        name = InkAction.parseName(command, 2);
        Cutscene cutscene = storyHandler.getPlayerSession().getScene().getCutsceneByName(name);
        if(cutscene == null) {
            return InkActionResult.error(this.getClass(), Translation.message("validation.cutscene", name).getString());
        }
        executeCutscene(cutscene);
        return InkActionResult.block();
    }

    private void executeCutscene(Cutscene cutscene) {
        KeyframeControllerBase keyframeControllerBase = storyHandler.getPlayerSession().getKeyframeControllerBase();
        if(keyframeControllerBase instanceof CameraAngleController cameraAngleController) {
            cameraAngleController.stopSession(false);
        }
        storyHandler.setCurrentDialogBox(null);
        ServerPlayer serverPlayer = Utils.getServerPlayerByUUID(Minecraft.getInstance().player.getUUID());
        CutsceneController cutsceneController = new CutsceneController(cutscene, serverPlayer, Playback.PlaybackType.PRODUCTION);
        cutsceneController.startSession();
        storyHandler.getPlayerSession().setKeyframeControllerBase(cutsceneController);
        CutscenePlayback cutscenePlayback = new CutscenePlayback(serverPlayer, cutscene.getKeyframeGroupList(), cutscene.getKeyframeGroupList().getFirst().getKeyframeList().getFirst(), cutsceneController);
        cutscenePlayback.setOnCutsceneEnd(() -> handleEndCutscene(cutsceneController));
        cutscenePlayback.start();
        sendDebugDetails();
    }

    private void handleEndCutscene(CutsceneController cutsceneController) {
        storyHandler.getPlayerSession().setSoloCam(cutsceneController.getCutscene().getKeyframeGroupList().getLast().getKeyframeList().getLast().getKeyframeCoordinate());
        if(storyHandler.getInkTagTranslators().getTagsToExecuteLater().isEmpty() && storyHandler.isFinished()) {
            storyHandler.stop(false);
        }
        storyHandler.getInkTagTranslators().executeLaterTags();

    }

    @Override
    void sendDebugDetails() {
        if(storyHandler.isDebugMode()) {
            Minecraft.getInstance().player.displayClientMessage(Translation.message("debug.cutscene", name), false);
        }
    }

    @Override
    public ErrorLine validate(String[] command, int line, String lineText, Scene scene) {
        if(command.length < 3) {
            return new ErrorLine(
                    line,
                    scene,
                    Translation.message("validation.missing_name").getString(),
                    lineText, false
            );
        }
        name = InkAction.parseName(command, 2);
        Cutscene cutscene = scene.getCutsceneByName(name);
        if(cutscene == null) {
            return new ErrorLine(
                    line,
                    scene,
                    Translation.message("validation.cutscene", name).getString(),
                    lineText, false
            );
        }
        return null;
    }
}
