package fr.loudo.narrativecraft.events;

import net.minecraft.server.MinecraftServer;

public class LifecycleEvent {

    public static void onServerStart(MinecraftServer server) {
        OnLifecycle.execute(server);
    }
}
