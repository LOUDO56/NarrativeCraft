package fr.loudo.narrativecraft.events;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class PlayerServerConnection {

    public static void onPlayerJoin(ServerGamePacketListenerImpl serverGamePacketListener, PacketSender packetSender, MinecraftServer server) {
        OnPlayerServerConnection.playerJoin(serverGamePacketListener.getPlayer());
    }

    public static void onPlayerLeave(ServerGamePacketListenerImpl serverGamePacketListener, MinecraftServer server) {
        OnPlayerServerConnection.playerLeave(serverGamePacketListener.getPlayer());

    }

}
