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
    private final int color;
    private final int hoverColor;
    private final int hoverWidth;
    private final boolean hoverBorder;

    public ChoiceButtonWidget(Choice choice) {
        super(0, 0, 0, 0, Component.literal(choice.getText()));
        Font font = Minecraft.getInstance().font;
        int width = font.width(choice.getText());
        int height = font.lineHeight;
        index = choice.getIndex();
        paddingX = 9;
        paddingY = 6;
        color = 0xFF000000;
        hoverBorder = true;
        hoverColor = 0xFFFFFFFF;
        hoverWidth = 1;
        this.setWidth(width + paddingX * 2);
        this.setHeight(height + paddingY * 2);
    }


    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int left = this.getX();
        int top = this.getY();
        int right = left + this.getWidth();
        int bottom = top + this.getHeight();

        if (this.isHovered && hoverBorder) {
            guiGraphics.fill(left - hoverWidth, top - hoverWidth, right + hoverWidth, bottom + hoverWidth, hoverColor);
        }

        guiGraphics.fill(left, top, right, bottom, color);

        guiGraphics.drawString(
                Minecraft.getInstance().font,
                this.getMessage().getString(),
                left + paddingX,
                top + paddingY + 1,
                0xFFFFFF,
                false
        );
    }


    @Override
    public void onPress() {
        Minecraft.getInstance().setScreen(null);
        StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
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
}
