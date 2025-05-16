package fr.loudo.narrativecraft.narrative.session;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PlayerSessionManager {

    private List<PlayerSession> playerSessions;

    public PlayerSessionManager() {
        this.playerSessions = new ArrayList<>();
    }

    public List<PlayerSession> getPlayerSessions() {
        return playerSessions;
    }

    public PlayerSession setSession(Player player, Chapter chapter, Scene scene) {
        ServerPlayer serverPlayer = Utils.getServerPlayerByUUID(player.getUUID());
        PlayerSessionManager playerSessionManager = NarrativeCraftMod.getInstance().getPlayerSessionManager();
        PlayerSession playerSession = playerSessionManager.getPlayerSession(serverPlayer);
        if(playerSession == null) {
            playerSession = new PlayerSession(serverPlayer, chapter, scene);
            playerSessionManager.getPlayerSessions().add(playerSession);
        } else {
            playerSession.setChapter(chapter);
            playerSession.setScene(scene);
        }
        return playerSession;
    }

    public PlayerSession getPlayerSession(ServerPlayer player) {
        for(PlayerSession playerSession : playerSessions) {
            if(playerSession.getPlayer().getName().getString().equals(player.getName().getString())) {
                return playerSession;
            }
        }
        return null;
    }

    public PlayerSession getPlayerSession(UUID uuid) {
        for(PlayerSession playerSession : playerSessions) {
            if(playerSession.getPlayer().getUUID().equals(uuid)) {
                return playerSession;
            }
        }
        return null;
    }

    public boolean clearSession(Player player) {
        for(PlayerSession playerSession : playerSessions) {
            if(playerSession.getPlayer().getName().getString().equals(player.getName().getString())) {
                playerSession.setChapter(null);
                playerSession.setScene(null);
                return true;
            }
        }
        return false;
    }
}
