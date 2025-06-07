package fr.loudo.narrativecraft.events;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.KeyframeControllerBase;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeGroup;
import fr.loudo.narrativecraft.narrative.dialog.Dialog;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;

public class OnRenderWorld {

    public static void renderWorld(PoseStack poseStack) {

        Dialog dialog = NarrativeCraftMod.getInstance().getTestDialog();
        if(dialog != null) {
            dialog.render(poseStack);
        }

        StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
        if(storyHandler != null && storyHandler.getCurrentDialogBox() != null) {
            storyHandler.getCurrentDialogBox().render(poseStack);
        }

        for(PlayerSession playerSession : NarrativeCraftMod.getInstance().getPlayerSessionManager().getPlayerSessions()) {
            KeyframeControllerBase keyframeControllerBase = playerSession.getKeyframeControllerBase();
            if(keyframeControllerBase == null) return;
            if(keyframeControllerBase.getPlaybackType() == Playback.PlaybackType.DEVELOPMENT) {
                if(keyframeControllerBase instanceof CutsceneController cutsceneController) {
                    if (cutsceneController.getCurrentPreviewKeyframe() == null) {
                        for (KeyframeGroup keyframeGroup : cutsceneController.getCutscene().getKeyframeGroupList()) {
                            keyframeGroup.showLineBetweenKeyframes(poseStack);
                        }
                    }
                }
            }
        }

//        matrix4fstack.popMatrix();
    }

}
