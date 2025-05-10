package fr.loudo.narrativecraft.events;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeGroup;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;

public class OnRenderWorld {

    public static void renderWorld(PoseStack poseStack) {
        for(PlayerSession playerSession : NarrativeCraftMod.getInstance().getPlayerSessionManager().getPlayerSessions()) {
            CutsceneController cutsceneController = playerSession.getCutsceneController();
            if (cutsceneController != null && cutsceneController.getCurrentPreviewKeyframe() == null) {
                for (KeyframeGroup keyframeGroup : cutsceneController.getCutscene().getKeyframeGroupList()) {
                    keyframeGroup.showLineBetweenKeyframes(poseStack);
                }
            }
        }
    }

}
