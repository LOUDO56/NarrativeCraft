package fr.loudo.narrativecraft.gui;

import net.minecraft.client.gui.Font;

public interface ICustomGuiRender {
    void drawnDialogSkip(float dialogWidth, float width, float height, float offsetX, int color);
    void drawStringFloat(String text, Font font, float x, float y, int color, boolean drawShadow);
}
