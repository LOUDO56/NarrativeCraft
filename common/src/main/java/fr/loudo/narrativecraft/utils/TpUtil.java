package fr.loudo.narrativecraft.utils;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class TpUtil {
    public static void teleportPlayer(ServerPlayer player, double x, double y, double z) {
        ServerLevel world = player.serverLevel();
        TeleportTransition teleportTransition = new TeleportTransition(
                world,
                new Vec3(x, y, z),
                new Vec3(0, 0, 0),
                player.getYRot(),
                player.getXRot(),
                false,
                false,
                Set.of(),
                TeleportTransition.DO_NOTHING
        );
        player.teleport(teleportTransition);
    }

    public static void teleportPlayer(ServerPlayer player, Vec3 position) {
        ServerLevel world = player.serverLevel();
        TeleportTransition teleportTransition = new TeleportTransition(
                world,
                new Vec3(position.x, position.y, position.z),
                new Vec3(0, 0, 0),
                player.getYRot(),
                player.getXRot(),
                false,
                false,
                Set.of(),
                TeleportTransition.DO_NOTHING
        );
        player.teleport(teleportTransition);
    }
}