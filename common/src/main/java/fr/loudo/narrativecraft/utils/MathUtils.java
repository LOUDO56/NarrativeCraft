package fr.loudo.narrativecraft.utils;

import net.minecraft.world.phys.Vec3;

public class MathUtils {

    public static double lerp(double v0, double v1, double t) {
        return v0 + t * (v1 - v0);
    }

}
