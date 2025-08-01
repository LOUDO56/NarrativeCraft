package fr.loudo.narrativecraft.narrative.dialog;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.gui.ICustomGuiRender;
import fr.loudo.narrativecraft.narrative.dialog.animations.DialogAnimationArrowSkip;
import fr.loudo.narrativecraft.narrative.dialog.animations.DialogAnimationScrollText;
import fr.loudo.narrativecraft.narrative.dialog.animations.DialogAppearAnimation2d;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.utils.Easing;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix3x2fStack;

public class Dialog2d extends DialogImpl {

    private final DialogAppearAnimation2d dialogAppearAnimation2d;
    private final DialogAnimationArrowSkip dialogAnimationArrowSkip;

    private int width, height, paddingX, paddingY, offset, textColor, backgroundColor;
    private boolean acceptNewDialog;
    private float scale;
    private String text;

    public Dialog2d(String text, int width, int height, int paddingX, int paddingY, float scale, float letterSpacing, float gap, int offset, int textColor, int backgroundColor) {
        this.text = text;
        this.width = width;
        this.height = height;
        this.paddingX = paddingX;
        this.paddingY = paddingY;
        this.offset = offset;
        this.scale = scale;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
        this.dialogAnimationScrollText = new DialogAnimationScrollText(
                text,
                letterSpacing,
                gap,
                width / 2,
                this
        );
        dialogAnimationArrowSkip = new DialogAnimationArrowSkip(this, 3.5f, 3.5f, 2f, -10f, 400L, 0xFFFFFF, 100, Easing.SMOOTH);
        this.dialogAppearAnimation2d = new DialogAppearAnimation2d(this);
        startTimeEnded = 0;
        dialogAutoSkipped = false;
    }

    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        // Init skip arrow instance BEFORE background,
        // or else when drawing dialog background AFTER dialog ended,
        // arrow is behind background, then above
        ((ICustomGuiRender)guiGraphics).drawnDialogSkip(
                0,
                0,
                0,
                0,
                0
        );
        int windowWidth = minecraft.getWindow().getGuiScaledWidth();
        int windowHeight = minecraft.getWindow().getGuiScaledHeight();

        int offsetDialog = offset;
        int guiScale = minecraft.options.guiScale().get();
        switch (guiScale) {
            case 1: offsetDialog *= 4;
            case 2: offsetDialog *= 2;
        }

        if (!dialogAppearAnimation2d.isAnimating() && endDialog && !dialogEnded) {
            dialogEnded = true;
            StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
            if(storyHandler != null) {
                storyHandler.setCurrentDialogBox(null);
                if(!unSkippable) {
                    storyHandler.showDialog();
                }
            }
        }

        Matrix3x2fStack poseStack = guiGraphics.pose();
        poseStack.pushMatrix();

        int dialogWidth = width + paddingX * 2;
        int dialogHeight = height + paddingY * 2;
        int centerX = windowWidth / 2;
        int centerY = windowHeight - offsetDialog - dialogHeight / 2;

        poseStack.translate(centerX, centerY);
        dialogAppearAnimation2d.render(poseStack, minecraft, acceptNewDialog ? DialogAppearAnimation2d.AppearType.DISAPPEAR : DialogAppearAnimation2d.AppearType.APPEAR);
        poseStack.translate(-dialogWidth / 2.0f, -dialogHeight / 2.0f);

        guiGraphics.fill(0, 0, dialogWidth, dialogHeight, backgroundColor);

        if(dialogAnimationScrollText.isFinished() && !endDialog) {
            poseStack.translate(0, dialogHeight - dialogAnimationArrowSkip.getHeight() - 5);
            if(!unSkippable) {
                dialogAnimationArrowSkip.render(guiGraphics, minecraft);
            }
            acceptNewDialog = true;
        }
        poseStack.popMatrix();

        if(!dialogAppearAnimation2d.isAnimating() && !endDialog) {
            dialogAnimationScrollText.render(guiGraphics, deltaTracker, scale);
        }

        if(dialogAnimationScrollText.isFinished() && forcedEndTime > 0  && !dialogAutoSkipped) {
            if(startTimeEnded == 0) startTimeEnded = System.currentTimeMillis();
            if(System.currentTimeMillis() - startTimeEnded >= forcedEndTime) {
                dialogAutoSkipped = true;
                NarrativeCraftMod.getInstance().getStoryHandler().next();
            }
        }
    }

    public void endDialog() {
        endDialog = true;
        dialogAppearAnimation2d.reset();
    }

    @Override
    public boolean isAnimating() {
        return dialogAppearAnimation2d.isAnimating();
    }

    public void reset() {
        dialogEnded = false;
        dialogAutoSkipped = false;
        startTimeEnded = 0;
        dialogAnimationArrowSkip.reset();
        if(acceptNewDialog) {
            dialogAnimationScrollText.reset();
        }
    }

    public int getOffset() {
        return offset;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getPaddingX() {
        return paddingX;
    }

    public void setPaddingX(int paddingX) {
        this.paddingX = paddingX;
    }

    public int getPaddingY() {
        return paddingY;
    }

    public void setPaddingY(int paddingY) {
        this.paddingY = paddingY;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        dialogAnimationScrollText.setText(text);
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setLetterSpacing(float letterSpacing) {
        dialogAnimationScrollText.setLetterSpacing(letterSpacing);
    }

    public void setGap(float gap) {
        dialogAnimationScrollText.setGap(gap);
    }
}
