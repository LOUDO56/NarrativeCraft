package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.KeyframeControllerBase;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngle;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeTrigger;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.screens.cameraAngles.CameraAngleCharacterScreen;
import fr.loudo.narrativecraft.screens.keyframes.KeyframeTriggerScreen;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class OnEntityRightClick {

    public static void entityRightClick(ServerPlayer player, Entity entity) {

        StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
        if(storyHandler != null && storyHandler.isRunning() && storyHandler.getStory() != null) return;

        PlayerSession playerSession = Utils.getSessionOrNull(Minecraft.getInstance().player.getUUID());
        if(playerSession == null) return;
        KeyframeControllerBase keyframeControllerBase = playerSession.getKeyframeControllerBase();
        if(keyframeControllerBase == null) return;

        if(keyframeControllerBase instanceof CutsceneController cutsceneController) {
            Keyframe keyframe = keyframeControllerBase.getKeyframeByEntity(entity);
            if(keyframe != null) {
                cutsceneController.setCurrentPreviewKeyframe(keyframe ,false);
            }
        } else if (keyframeControllerBase instanceof CameraAngleController cameraAngleController) {
            CameraAngle cameraAngle = cameraAngleController.getKeyframeByEntity(entity);
            if(cameraAngle != null) {
                cameraAngleController.setCurrentPreviewKeyframe(cameraAngle);
            }
        }

        if(keyframeControllerBase instanceof CameraAngleController cameraAngleController) {
            if(cameraAngleController.isEntityInController(entity)) {
                CharacterStoryData characterStoryData = cameraAngleController.getCharacterDataByEntity(entity);
                CameraAngleCharacterScreen screen = new CameraAngleCharacterScreen(characterStoryData, cameraAngleController);
                Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(screen));
            }
        }
        if(keyframeControllerBase instanceof CutsceneController cutsceneController) {
            Animation animation = cutsceneController.getAnimationFromEntity(entity);
            if(animation != null) {
                CameraAngleCharacterScreen screen = new CameraAngleCharacterScreen(animation, cutsceneController);
                Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(screen));
            }

            KeyframeTrigger keyframeTrigger = cutsceneController.getKeyframeTriggerByEntity(entity);
            if(keyframeTrigger != null) {
                KeyframeTriggerScreen screen = new KeyframeTriggerScreen(cutsceneController, keyframeTrigger);
                Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(screen));
            }

        }

    }

}
