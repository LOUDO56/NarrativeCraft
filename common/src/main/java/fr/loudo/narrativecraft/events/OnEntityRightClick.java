package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.narrative.chapter.scenes.KeyframeControllerBase;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.screens.cameraAngles.CameraAngleCharacterScreen;
import fr.loudo.narrativecraft.utils.FakePlayer;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class OnEntityRightClick {

    public static void entityRightClick(ServerPlayer player, Entity entity) {

        PlayerSession playerSession = Utils.getSessionOrNull(player);
        if(playerSession == null) return;
        KeyframeControllerBase keyframeControllerBase = playerSession.getKeyframeControllerBase();
        if(keyframeControllerBase == null) return;

        Keyframe keyframe = keyframeControllerBase.getKeyframeByEntity(entity);
        if(keyframe != null) {
            if(keyframeControllerBase instanceof CutsceneController cutsceneController) {
                cutsceneController.setCurrentPreviewKeyframe(keyframe ,false);
            } else if (keyframeControllerBase instanceof CameraAngleController cameraAngleController) {
                cameraAngleController.setCurrentPreviewKeyframe(keyframe);
            }
        }
        if(keyframeControllerBase instanceof CameraAngleController cameraAngleController) {
            if(cameraAngleController.isEntityInController(entity)) {
                CameraAngleCharacterScreen screen = new CameraAngleCharacterScreen(entity, cameraAngleController);
                Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(screen));
            }
        }

    }

}
