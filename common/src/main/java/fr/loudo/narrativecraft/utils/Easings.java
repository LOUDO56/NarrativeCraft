package fr.loudo.narrativecraft.utils;

public class Easings {

    public static double smooth(double t) {
        return t * t * (3 - 2 * t);
    }

    public static double linear(double t) {
        return t;
    }
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

    public static double easeOutElastic(double t) {
        double c4 = (2 * Math.PI) / 3;

        if (t == 0) {
            return 0;
        } else if (t == 1) {
            return 1;
        } else {
            return Math.pow(2, -10 * t) * Math.sin((t * 10 - 0.75) * c4) + 1;
        }
    }

}
