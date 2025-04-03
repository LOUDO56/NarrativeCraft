package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@Mod(NarrativeCraftMod.MOD_ID)
public class PlayerServerConnection {

    public PlayerServerConnection(IEventBus eventBus) {
        NeoForge.EVENT_BUS.addListener(PlayerServerConnection::onPlayerJoin);
        NeoForge.EVENT_BUS.addListener(PlayerServerConnection::onPlayerLeave);
    }

    private static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        OnPlayerServerConnection.playerJoin((ServerPlayer) event.getEntity());
    }

    private static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        OnPlayerServerConnection.playerLeave((ServerPlayer) event.getEntity());

    }
}
