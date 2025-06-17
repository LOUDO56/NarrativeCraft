package fr.loudo.narrativecraft.utils;

import java.util.List;

public enum Easing {
    SMOOTH, LINEAR, EASE_IN, EASE_OUT, EASE_IN_OUT;

    public static double getInterpolation(Easing easing, double t) {
        switch (easing) {
            case SMOOTH -> {
                return t * t * t * (t * (6 * t - 15) + 10);
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

    public static String getEasingsString() {
        StringBuilder s = new StringBuilder();
        List<Easing> easings = getEasings();
        for (int i = 0; i < easings.size(); i++) {
            Easing easing = easings.get(i);
            if(i == easings.size() - 2) {
                s.append(easing.name().toLowerCase()).append(" and ").append(easings.getLast().name().toLowerCase());
                break;
            } else {
                s.append(easing.name().toLowerCase()).append(", ");
            }
        }
        return s.toString();
    }

}
