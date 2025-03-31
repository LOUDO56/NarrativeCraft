package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraft;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.session.PlayerSessionManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NarrativeCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerServerConnection {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        PlayerSessionManager playerSessionManager = NarrativeCraft.getInstance().getPlayerSessionManager();
        ServerPlayer player = (ServerPlayer) event.getEntity();
        if(playerSessionManager.getPlayerSession(player) == null) {
            PlayerSession playerSession = new PlayerSession(player);
            playerSessionManager.getPlayerSessions().add(playerSession);
        }
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        PlayerSessionManager playerSessionManager = NarrativeCraft.getInstance().getPlayerSessionManager();
        ServerPlayer player = (ServerPlayer) event.getEntity();
        PlayerSession playerSession = playerSessionManager.getPlayerSession(player);
        if(playerSession != null) {
            playerSessionManager.getPlayerSessions().remove(playerSession);
        }
        Recording recording = NarrativeCraft.getInstance().getRecordingHandler().getRecordingOfPlayer(player);
        if(recording != null) {
            recording.stop();
        }

    }
}
