package fr.loudo.narrativecraft.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.toasts.SystemToast;
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

    public static float getPixelValue(float value, float scale) {
        return (value * scale) / Math.max(1, Minecraft.getInstance().options.guiScale().get());
    }

    public static void sendToast(Component name, Component description) {
        Minecraft.getInstance().getToastManager().addToast(
                new SystemToast(
                        SystemToast.SystemToastId.NARRATOR_TOGGLE,
                        name,
                        description
                )
        );
    }

    public static class LabelBox {

        private final StringWidget stringWidget;
        private final EditBox editBox;
        private final Align align;

        public LabelBox(Component text, Font font, int width, int height, int x, int y, Align align) {
            int yStringWidget = y;
            if(align == Align.HORIZONTAL) {
                y += height / 2;
                yStringWidget = y - font.lineHeight / 2;
            }
            stringWidget = ScreenUtils.text(text, font, x, yStringWidget);
            if(align == Align.HORIZONTAL) {
                x = stringWidget.getX() + stringWidget.getWidth() + 5;
                y -= height / 2;
            } else if (align == Align.VERTICAL) {
                y += font.lineHeight + 5;
            }
            editBox = new EditBox(
                    font,
                    x,
                    y,
                    width,
                    height,
                    Component.literal(text + " value")
            );
            this.align = align;
        }

        public void setPosition(int x, int y) {
            stringWidget.setPosition(x, y);
            if(align == Align.HORIZONTAL) {
                x += 5;
                y = stringWidget.getY() - editBox.getHeight() / 2;
            } else if (align == Align.VERTICAL) {
                y = stringWidget.getY() + stringWidget.getHeight() + 5;
            }
            editBox.setPosition(x, y);
        }

        public StringWidget getStringWidget() {
            return stringWidget;
        }

        public EditBox getEditBox() {
            return editBox;
        }

    }

    public static class MultilineLabelBox {

        private final StringWidget stringWidget;
        private final MultiLineEditBox multiLineEditBox;

        public MultilineLabelBox(Component text, Font font, int width, int height, int x, int y, Component placeholder) {
            stringWidget = ScreenUtils.text(text, font, x, y);
            multiLineEditBox = new MultiLineEditBox(
                    font,
                    x,
                    y + stringWidget.getHeight() + 5,
                    width,
                    height,
                    placeholder,
                    Component.literal("")
            );
        }

        public void setPosition(int x, int y) {
            stringWidget.setPosition(x, y);
            y += stringWidget.getHeight() + 5;
            multiLineEditBox.setPosition(x, y);
        }

        public StringWidget getStringWidget() {
            return stringWidget;
        }

        public MultiLineEditBox getMultiLineEditBox() {
            return multiLineEditBox;
        }
    }

    public enum Align {
        VERTICAL,
        HORIZONTAL
    }

}
