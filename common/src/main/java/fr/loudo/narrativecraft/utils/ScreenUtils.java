package fr.loudo.narrativecraft.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;

public class ScreenUtils {

    public static StringWidget text(Component text, Font font, int x, int y) {
        return new StringWidget(x, y, font.width(text.getVisualOrderText()), 9, text, font);
    }

    public static StringWidget text(Component text, Font font, int x, int y, int color) {
        StringWidget stringWidget = new StringWidget(x, y, font.width(text.getVisualOrderText()), 9, text, font);
        stringWidget.setColor(color);
        return stringWidget;
    }
}
