package fr.loudo.narrativecraft.registers;

import fr.loudo.narrativecraft.events.BlockBreakEvent;
import fr.loudo.narrativecraft.events.LifecycleEvent;
import fr.loudo.narrativecraft.events.PlayerServerConnection;
import fr.loudo.narrativecraft.events.ServerTickEvent;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class EventsRegister {

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(LifecycleEvent::onServerStart);
        ServerPlayConnectionEvents.JOIN.register(PlayerServerConnection::onPlayerJoin);
        ServerPlayConnectionEvents.DISCONNECT.register(PlayerServerConnection::onPlayerLeave);
        ServerTickEvents.END_SERVER_TICK.register(ServerTickEvent::onServerTick);
        PlayerBlockBreakEvents.AFTER.register(BlockBreakEvent::onBlockBreak);
    }

}
