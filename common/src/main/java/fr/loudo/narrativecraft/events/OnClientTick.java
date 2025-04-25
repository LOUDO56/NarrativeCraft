package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.keys.ModKeys;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class OnClientTick {

    public static void clientTick(Minecraft client) {

        if(client.player == null) return;

        PlayerSession playerSession = Utils.getSessionOrNull(client.player.getUUID());
        if(playerSession == null) return;

        CutsceneController cutsceneController = playerSession.getCutsceneController();
        if(cutsceneController != null) {
            ModKeys.handleKeyPress(ModKeys.CREATE_KEYFRAME_GROUP, () -> {
                cutsceneController.createKeyframeGroup();
                playerSession.getPlayer().playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.MASTER, 0.3F, 2);
            });
            ModKeys.handleKeyPress(ModKeys.ADD_KEYFRAME, () -> {
                playerSession.getPlayer().playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.MASTER, 0.3F, 2);
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
        }
    }

}
