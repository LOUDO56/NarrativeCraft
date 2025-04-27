package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.items.CutsceneEditItems;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.session.PlayerSessionManager;
import net.minecraft.server.level.ServerPlayer;

public class OnPlayerServerConnection {

    public static void playerJoin(ServerPlayer player) {
        PlayerSessionManager playerSessionManager = NarrativeCraftMod.getInstance().getPlayerSessionManager();
        if(playerSessionManager.getPlayerSession(player) == null) {
            PlayerSession playerSession = new PlayerSession(player);
            playerSessionManager.getPlayerSessions().add(playerSession);
        }
        CutsceneEditItems.init(player.registryAccess());
    }

    public static void playerLeave(ServerPlayer player) {
        PlayerSessionManager playerSessionManager = NarrativeCraftMod.getInstance().getPlayerSessionManager();
        PlayerSession playerSession = playerSessionManager.getPlayerSession(player);
        if(playerSession != null) {
            playerSessionManager.getPlayerSessions().remove(playerSession);
        }
        Recording recording = NarrativeCraftMod.getInstance().getRecordingHandler().getRecordingOfPlayer(player);
        if(recording != null) {
            recording.stop();
        }

    }

}
