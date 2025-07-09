package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.recordings.MovementData;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.actions.ActionsData;
import fr.loudo.narrativecraft.narrative.recordings.actions.RespawnAction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class OnRespawn {

    public static void respawn(Player player) {
        Recording recording = NarrativeCraftMod.getInstance().getRecordingHandler().getRecordingOfPlayer(player);
        if(recording != null && recording.isRecording()) {
            MovementData respawnLocation = new MovementData(
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    player.getXRot(),
                    player.getYRot(),
                    player.getYHeadRot(),
                    player.onGround()
            );
            RespawnAction action = new RespawnAction(recording.getTick(), respawnLocation);
            ActionsData actionsData = recording.getActionDataFromEntity(player);
            actionsData.addAction(action);
            for(ServerPlayer serverPlayer : NarrativeCraftMod.server.getPlayerList().getPlayers()) {
                if(serverPlayer.getUUID().equals(player.getUUID())) {
                    actionsData.setEntity(serverPlayer);
                }
            }

        }
    }

}
