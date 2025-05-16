package fr.loudo.narrativecraft.events;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.KeyframeControllerBase;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeGroup;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;

public class OnRenderWorld {

    public static void renderWorld(PoseStack poseStack) {
        for(PlayerSession playerSession : NarrativeCraftMod.getInstance().getPlayerSessionManager().getPlayerSessions()) {
            KeyframeControllerBase keyframeControllerBase = playerSession.getKeyframeControllerBase();
            if(keyframeControllerBase == null) return;
            if(keyframeControllerBase instanceof CutsceneController cutsceneController) {
                if (cutsceneController.getCurrentPreviewKeyframe() == null) {
                    for (KeyframeGroup keyframeGroup : cutsceneController.getCutscene().getKeyframeGroupList()) {
                        keyframeGroup.showLineBetweenKeyframes(poseStack);
                    }
                }
            }
        }
    }

}
