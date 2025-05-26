package fr.loudo.narrativecraft.screens.choices;

import com.bladecoder.ink.runtime.Choice;
import fr.loudo.narrativecraft.screens.components.ChoiceButtonWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ChoicesScreen extends Screen {

    private final List<Choice> choiceList;

    public ChoicesScreen(List<Choice> choiceList) {
        super(Component.literal("Choice screen"));
        this.choiceList = choiceList;
    }

    @Override
    protected void init() {
        List<ChoiceButtonWidget> choiceButtonWidgetList = new ArrayList<>();
        for(Choice choice : choiceList) {
            choiceButtonWidgetList.add(new ChoiceButtonWidget(
                choice
            ));
        }
        int spacing = 10;
        int baseY = 80;
        int maxWidthUpDown = 0;
        for (int i = 0; i < choiceButtonWidgetList.size(); i++) {
            if(i % 2 != 0) {
                if(choiceButtonWidgetList.get(i).getWidth() > maxWidthUpDown) {
                    maxWidthUpDown = choiceButtonWidgetList.get(i).getWidth();
                }
            }
        }
        for (int i = 0; i < choiceButtonWidgetList.size(); i++) {
            ChoiceButtonWidget choiceButtonWidget = choiceButtonWidgetList.get(i);
            int currentX = 0;
            int currentY = this.height - baseY;
            if(choiceButtonWidgetList.size() == 4) currentY -= baseY;
            switch (i) {
                case 0:
                    if(choiceButtonWidgetList.size() > 2) {
                        currentX = this.width / 2 - choiceButtonWidget.getWidth() - maxWidthUpDown / 2;
                    } else {
                        currentX = this.width / 2 - choiceButtonWidget.getWidth() - spacing;
                    }
                    break;
                case 1:
                    if(choiceButtonWidgetList.size() > 2) {
                        currentY -= choiceButtonWidget.getHeight() + spacing;
                        currentX = this.width / 2 - choiceButtonWidget.getWidth() / 2;
                    } else {
                        currentX = this.width / 2 + spacing;
                    }
                    break;
                case 2:
                    currentX = this.width / 2 + maxWidthUpDown / 2;
                    break;
                case 3:
                    currentY += choiceButtonWidget.getHeight() + spacing;
                    currentX = this.width / 2 - choiceButtonWidget.getWidth() / 2;
                    break;
            }
            choiceButtonWidget.setX(currentX);
            choiceButtonWidget.setY(currentY);
            this.addRenderableWidget(choiceButtonWidget);
        }
    }

    @Override
    public void onClose() {}

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void renderBlurredBackground() {}

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}
}
