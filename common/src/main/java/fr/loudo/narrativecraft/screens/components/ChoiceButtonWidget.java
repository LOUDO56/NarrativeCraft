package fr.loudo.narrativecraft.screens.components;

import com.bladecoder.ink.runtime.Choice;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class ChoiceButtonWidget extends AbstractButton {

    private final int index;
    private final int paddingX;
    private final int paddingY;
    private final int hoverWidth;
    private final boolean hoverBorder;
    private int backgroundColor, textColor, hoverColor;
    private boolean canPress;

    public ChoiceButtonWidget(Choice choice) {
        super(0, 0, 0, 0, Component.literal(choice.getText()));
        Font font = Minecraft.getInstance().font;
        int width = font.width(choice.getText());
        int height = font.lineHeight;
        index = choice.getIndex();
        paddingX = 9;
        paddingY = 6;
        backgroundColor = 0x000000;
        textColor = 0xFFFFFF;
        hoverColor = 0xFFFFFF;
        hoverBorder = true;
        hoverWidth = 1;
        canPress = true;
        this.setWidth(width + paddingX * 2);
        this.setHeight(height + paddingY * 2);
    }


    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int left = this.getX();
        int top = this.getY();
        int right = left + this.getWidth();
        int bottom = top + this.getHeight();

        if (this.isHovered && hoverBorder && canPress) {
            guiGraphics.fill(left - hoverWidth, top - hoverWidth, right + hoverWidth, bottom + hoverWidth, hoverColor);
        }

        guiGraphics.fill(left, top, right, bottom, backgroundColor);

        guiGraphics.drawString(
                Minecraft.getInstance().font,
                this.getMessage().getString(),
                left + paddingX,
                top + paddingY + 1,
                textColor,
                false
        );
    }


    @Override
    public void onPress() {
        if(!canPress) return;
        Minecraft.getInstance().setScreen(null);
        StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
        if(storyHandler == null) return;
        try {
            storyHandler.getStory().chooseChoiceIndex(index);
            storyHandler.getCurrentChoices().clear();
            storyHandler.next();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.active && this.visible) {
            if (this.isValidClickButton(button)) {
                boolean flag = this.isMouseOver(mouseX, mouseY);
                if (flag) {
                    this.onClick(mouseX, mouseY);
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    public void setOpacity(int opacity) {
        backgroundColor = (opacity << 24) | (backgroundColor & 0x00FFFFFF);
        textColor = (opacity << 24) | (textColor & 0x00FFFFFF);
        hoverColor = (opacity << 24) | (hoverColor & 0x00FFFFFF);
    }

    public boolean isCanPress() {
        return canPress;
    }

    public void setCanPress(boolean canPress) {
        this.canPress = canPress;
    }
}
