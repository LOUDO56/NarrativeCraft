package fr.loudo.narrativecraft.utils;

public class ColorUtils {

    public static int ARGB(int alpha, int red, int green, int blue) {
        return  (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    public static int AHEX(int alpha, int hex) {
        return (alpha << 24) | (hex & 0x00FFFFFF);
    }


}
