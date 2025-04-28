package fr.loudo.narrativecraft.utils;

import java.util.List;

public enum Easing {
    SMOOTH, LINEAR, EASE_IN, EASE_OUT, EASE_IN_OUT;

    public static double getInterpolation(Easing easing, double t) {
        switch (easing) {
            case SMOOTH -> {
                return t * t * (3 - 2 * t);
            }
            case LINEAR -> {
                return t;
            }
            case EASE_IN -> {
                return t * t;
            }
            case EASE_OUT -> {
                return t * (2 - t);
            }
            case EASE_IN_OUT -> {
                if (t < 0.5) {
                    return 2 * t * t;
                } else {
                    return -1 + (4 - 2 * t) * t;
                }
            }
        }
        return t;
    }

    public static List<Easing> getEasings() {
        return List.of(SMOOTH, LINEAR, EASE_IN, EASE_OUT, EASE_IN_OUT);
    }

}
