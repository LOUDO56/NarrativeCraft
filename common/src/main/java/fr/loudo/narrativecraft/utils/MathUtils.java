package fr.loudo.narrativecraft.utils;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class MathUtils {

    public static double lerp(double v0, double v1, double t) {
        return v0 + t * (v1 - v0);
    }

    public static Vec3 lerp(double delta, Vec3 start, Vec3 end) {
        return new Vec3(Mth.lerp(delta, start.x, end.x), Mth.lerp(delta, start.y, end.y), Mth.lerp(delta, start.z, end.z));
    }

    public static float getSecondsByMillis(long milli) {
        if(milli == 0) return 0;
        return milli / 1000f;
    }

    public static long getMillisBySecond(double second) {
        return (long) (second * 1000L);
    }

    public static float get360Angle(float byteAngle) {
        return (byteAngle + 360) % 360;
    }

    public static float get180Angle(float angle) {
        return (angle + 180) % 360 - 180;
    }

    public static float getRandomFloat(float min, float max) {
        Random r = new Random();
        return min + r.nextFloat() * (max - min);
    }

}
