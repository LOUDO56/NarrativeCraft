package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.keys.ModKeys;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeGroup;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
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

        CutsceneController cutsceneController = playerSession.getCutsceneController();
        if(cutsceneController != null) {
            ModKeys.handleKeyPress(ModKeys.CREATE_KEYFRAME_GROUP, () -> {
                KeyframeGroup keyframeGroup = cutsceneController.createKeyframeGroup();
                Minecraft.getInstance().player.displayClientMessage(Translation.message("cutscene.keyframegroup.created", keyframeGroup.getId()), false);
            });
            ModKeys.handleKeyPress(ModKeys.ADD_KEYFRAME, () -> {
                if (cutsceneController.addKeyframe()) {
                    playerSession.getPlayer().sendSystemMessage(
                            Translation.message("cutscene.keyframe.added", playerSession.getCutsceneController().getSelectedKeyframeGroup().getId())
                    );
                } else {
                    playerSession.getPlayer().sendSystemMessage(
                            Translation.message("cutscene.keyframe.added.fail")
                    );
                }
            });
            ModKeys.handleKeyPress(ModKeys.CUTSCENE_CONTROLLER_SCREEN, () -> {
                CutsceneControllerScreen screen = new CutsceneControllerScreen(playerSession.getCutsceneController());
                client.execute(() -> client.setScreen(screen));
            });
        }
    }

}
