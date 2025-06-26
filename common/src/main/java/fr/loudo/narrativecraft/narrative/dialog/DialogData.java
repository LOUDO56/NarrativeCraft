package fr.loudo.narrativecraft.narrative.dialog;

import net.minecraft.world.phys.Vec2;

public class DialogData {

    public static DialogData globalDialogData;

    private String characterName;
    private String text;
    private Vec2 offset;
    private int textColor;
    private int backgroundColor;
    private float paddingX;
    private float paddingY;
    private float scale;
    private float letterSpacing;
    private float gap;
    private int maxWidth;
    private boolean unSkippable;
    private long endForceEndTime;
    private float bobbingNoiseShakeSpeed;
    private float bobbingNoiseShakeStrength;

    public DialogData(String characterName, String text, Vec2 offset, int textColor, int backgroundColor,
                          float paddingX, float paddingY, float scale, float letterSpacing, float gap,
                          int maxWidth, boolean unSkippable, long endForceEndTime, float bobbingNoiseShakeSpeed, float bobbingNoiseShakeStrength) {
        this.characterName = characterName;
        this.text = text;
        this.offset = offset;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
        this.paddingX = paddingX;
        this.paddingY = paddingY;
        this.scale = scale;
        this.letterSpacing = letterSpacing;
        this.gap = gap;
        this.maxWidth = maxWidth;
        this.unSkippable = unSkippable;
        this.endForceEndTime = endForceEndTime;
        this.bobbingNoiseShakeSpeed = bobbingNoiseShakeSpeed;
        this.bobbingNoiseShakeStrength = bobbingNoiseShakeStrength;
    }

    public DialogData(DialogData dialogData) {
        this.characterName = dialogData.characterName;
        this.text = dialogData.text;
        this.offset = dialogData.offset;
        this.textColor = dialogData.textColor;
        this.backgroundColor = dialogData.backgroundColor;
        this.paddingX = dialogData.paddingX;
        this.paddingY = dialogData.paddingY;
        this.scale = dialogData.scale;
        this.letterSpacing = dialogData.letterSpacing;
        this.gap = dialogData.gap;
        this.maxWidth = dialogData.maxWidth;
        this.unSkippable = dialogData.unSkippable;
        this.endForceEndTime = dialogData.endForceEndTime;
        this.bobbingNoiseShakeSpeed = dialogData.bobbingNoiseShakeSpeed;
        this.bobbingNoiseShakeStrength = dialogData.bobbingNoiseShakeStrength;
    }

    public String getCharacterName() { return characterName; }
    public void setCharacterName(String characterName) { this.characterName = characterName; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Vec2 getOffset() { return offset; }
    public void setOffset(Vec2 offset) { this.offset = offset; }

    public int getTextColor() { return textColor; }
    public void setTextColor(int textColor) { this.textColor = textColor; }

    public int getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(int backgroundColor) { this.backgroundColor = backgroundColor; }

    public float getPaddingX() { return paddingX; }
    public void setPaddingX(float paddingX) { this.paddingX = paddingX; }

    public float getPaddingY() { return paddingY; }
    public void setPaddingY(float paddingY) { this.paddingY = paddingY; }

    public float getScale() { return scale; }
    public void setScale(float scale) { this.scale = scale; }

    public float getLetterSpacing() { return letterSpacing; }
    public void setLetterSpacing(float letterSpacing) { this.letterSpacing = letterSpacing; }

    public float getGap() { return gap; }
    public void setGap(float gap) { this.gap = gap; }

    public int getMaxWidth() { return maxWidth; }
    public void setMaxWidth(int maxWidth) { this.maxWidth = maxWidth; }

    public boolean isUnSkippable() { return unSkippable; }
    public void setUnSkippable(boolean unSkippable) { this.unSkippable = unSkippable; }

    public long getEndForceEndTime() { return endForceEndTime; }
    public void setEndForceEndTime(long endForceEndTime) { this.endForceEndTime = endForceEndTime; }

    public float getBobbingNoiseShakeSpeed() {return bobbingNoiseShakeSpeed;}
    public void setBobbingNoiseShakeSpeed(float bobbingNoiseShakeSpeed) {this.bobbingNoiseShakeSpeed = bobbingNoiseShakeSpeed;}

    public float getBobbingNoiseShakeStrength() {return bobbingNoiseShakeStrength;}
    public void setBobbingNoiseShakeStrength(float bobbingNoiseShakeStrength) {this.bobbingNoiseShakeStrength = bobbingNoiseShakeStrength;}
}


