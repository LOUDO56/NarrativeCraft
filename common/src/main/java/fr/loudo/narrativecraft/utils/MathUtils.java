package fr.loudo.narrativecraft.utils;

import net.minecraft.world.phys.Vec3;

public class MathUtils {

    public static double lerp(double v0, double v1, double t) {
        return v0 + t * (v1 - v0);
    }

    public static Vec3 getNextLocation(Vec3 firstPos, Vec3 secondPost, double t) {

        double x = lerp(firstPos.x(), secondPost.x(), t);
        double y = lerp(firstPos.y(), secondPost.y(), t);
        double z = lerp(firstPos.y(), secondPost.z(), t);

        return new Vec3(x, y, z);

    }

}
