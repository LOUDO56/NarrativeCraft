package fr.loudo.narrativecraft.events;

import net.minecraft.server.level.ServerPlayer;

public class RespawnEvent {

    public static void onRespawn(ServerPlayer player, ServerPlayer player1, boolean b) {
        OnRespawn.respawn(player);
    }
}
