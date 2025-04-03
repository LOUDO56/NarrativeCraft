package fr.loudo.narrativecraft.narrative.session;

import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class PlayerSessionManager {

    private List<PlayerSession> playerSessions;

    public PlayerSessionManager() {
        this.playerSessions = new ArrayList<>();
    }

    public List<PlayerSession> getPlayerSessions() {
        return playerSessions;
    }

    public PlayerSession getPlayerSession(ServerPlayer player) {
        for(PlayerSession playerSession : playerSessions) {
            if(playerSession.getPlayer().getName().getString().equals(player.getName().getString())) {
                return playerSession;
            }
        }
        return null;
    }
}
