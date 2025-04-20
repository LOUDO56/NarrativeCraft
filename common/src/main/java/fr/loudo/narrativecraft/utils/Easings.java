package fr.loudo.narrativecraft.utils;

public class Easings {

    public static double easeIn(double t) {
        return t * t;
    }

    public static double easeOut(double t) {
        return t * (2 - t);
    }

    public static double easeInOut(double t) {
        if (t < 0.5) {
            return 2 * t * t;
        } else {
            return -1 + (4 - 2 * t) * t;
        }
    }
}
