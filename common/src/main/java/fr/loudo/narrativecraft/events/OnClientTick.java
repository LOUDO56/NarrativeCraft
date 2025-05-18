package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.keys.ModKeys;
import fr.loudo.narrativecraft.narrative.chapter.scenes.KeyframeControllerBase;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeGroup;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.screens.cameraAngles.CameraAngleControllerScreen;
import fr.loudo.narrativecraft.screens.cameraAngles.CameraAngleInfoKeyframeScreen;
import fr.loudo.narrativecraft.screens.cutscenes.CutsceneControllerScreen;
import fr.loudo.narrativecraft.screens.storyManager.chapters.ChaptersScreen;
import fr.loudo.narrativecraft.screens.storyManager.scenes.ScenesMenuScreen;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class OnClientTick {

    public static void clientTick(Minecraft client) {

        if(client.player == null) return;

        ModKeys.handleKeyPress(ModKeys.OPEN_STORY_MANAGER, () -> {
            Screen screen;
            PlayerSession playerSession = Utils.getSessionOrNull(client.player.getUUID());
            if(playerSession == null) {
                screen = new ChaptersScreen();
            } else {
                screen = new ScenesMenuScreen(playerSession.getScene());
            }
            client.execute(() -> client.setScreen(screen));
        });

        PlayerSession playerSession = Utils.getSessionOrNull(client.player.getUUID());
        if(playerSession == null) return;

        ModKeys.handleKeyPress(ModKeys.NEXT_DIALOG, () -> {
            NarrativeCraftMod.getInstance().getStoryHandler().next();
        });

        KeyframeControllerBase keyframeControllerBase = playerSession.getKeyframeControllerBase();
        if(keyframeControllerBase == null) return;
        if(keyframeControllerBase instanceof CutsceneController cutsceneController) {
            ModKeys.handleKeyPress(ModKeys.CREATE_KEYFRAME_GROUP, () -> {
                KeyframeGroup keyframeGroup = cutsceneController.createKeyframeGroup();
                Minecraft.getInstance().player.displayClientMessage(Translation.message("cutscene.keyframegroup.created", keyframeGroup.getId()), false);
            });
            ModKeys.handleKeyPress(ModKeys.ADD_KEYFRAME, () -> {
                if (cutsceneController.addKeyframe()) {
                    playerSession.getPlayer().sendSystemMessage(
                            Translation.message("cutscene.keyframe.added", cutsceneController.getSelectedKeyframeGroup().getId())
                    );
                } else {
                    playerSession.getPlayer().sendSystemMessage(
                            Translation.message("cutscene.keyframe.added.fail")
                    );
                }
            });
            ModKeys.handleKeyPress(ModKeys.OPEN_KEYFRAME_EDIT_SCREEN, () -> {
                CutsceneControllerScreen screen = new CutsceneControllerScreen(cutsceneController);
                client.execute(() -> client.setScreen(screen));
            });
        }
        if(keyframeControllerBase instanceof CameraAngleController cameraAngleController) {
            ModKeys.handleKeyPress(ModKeys.ADD_KEYFRAME, () -> {
                CameraAngleInfoKeyframeScreen screen = new CameraAngleInfoKeyframeScreen(cameraAngleController);
                client.execute(() -> client.setScreen(screen));
            });
            ModKeys.handleKeyPress(ModKeys.OPEN_KEYFRAME_EDIT_SCREEN, () -> {
                CameraAngleControllerScreen screen = new CameraAngleControllerScreen(cameraAngleController);
                client.execute(() -> client.setScreen(screen));
            });
        }
    }

}
