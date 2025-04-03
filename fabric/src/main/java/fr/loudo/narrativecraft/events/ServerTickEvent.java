package fr.loudo.narrativecraft.events;

import net.minecraft.server.MinecraftServer;

public class ServerTickEvent {

    public static void onServerTick(MinecraftServer server) {
        OnServerTick.serverTick();
    }
}
