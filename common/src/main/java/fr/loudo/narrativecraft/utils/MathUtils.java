package fr.loudo.narrativecraft.utils;

public class MathUtils {

    public static double lerp(double v0, double v1, double t) {
        return v0 + t * (v1 - v0);
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

}
