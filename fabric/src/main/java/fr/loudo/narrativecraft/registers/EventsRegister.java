package fr.loudo.narrativecraft.registers;

import fr.loudo.narrativecraft.events.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class EventsRegister {

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(LifecycleEvent::onServerStart);
        ServerPlayConnectionEvents.JOIN.register(PlayerServerConnection::onPlayerJoin);
        ServerPlayConnectionEvents.DISCONNECT.register(PlayerServerConnection::onPlayerLeave);
        ServerTickEvents.END_SERVER_TICK.register(ServerTickEvent::onServerTick);
        PlayerBlockBreakEvents.AFTER.register(BlockBreakEvent::onBlockBreak);
        UseBlockCallback.EVENT.register(RightClickBlock::onRightClickBlock);
        UseEntityCallback.EVENT.register(EntityRightClick::onEntityRightClick);
        ClientTickEvents.END_CLIENT_TICK.register(OnClientTick::clientTick);
    }

}
