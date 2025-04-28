package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class OnEntityRightClick {

    public static void entityRightClick(ServerPlayer player, Entity entity) {

        PlayerSession playerSession = Utils.getSessionOrNull(player);
        if(playerSession != null && playerSession.getCutsceneController() != null) {
            Keyframe keyframe = playerSession.getCutsceneController().getKeyframeByEntity(entity);
            if(keyframe != null) {
                playerSession.getCutsceneController().setCurrentPreviewKeyframe(keyframe ,false);
            }
        }

    }

}
