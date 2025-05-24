package fr.loudo.narrativecraft.utils;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class TpUtil {
    public static void teleportPlayer(ServerPlayer player, double x, double y, double z) {
        player.teleportTo(x, y, z);
    }

    public static void teleportPlayer(ServerPlayer player, Vec3 position) {
        player.teleportTo(position.x, position.y, position.z);
    }
}