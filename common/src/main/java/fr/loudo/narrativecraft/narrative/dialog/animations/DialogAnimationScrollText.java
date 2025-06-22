package fr.loudo.narrativecraft.narrative.dialog.animations;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.mixin.fields.FontFields;
import fr.loudo.narrativecraft.narrative.dialog.Dialog;
import fr.loudo.narrativecraft.narrative.dialog.DialogAnimationType;
import fr.loudo.narrativecraft.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Random;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DialogAnimationScrollText {

    private final long showLetterDelay = 30L;
    private final float offset = 4.1f;
    private final Dialog dialog;

    private int maxWidth;
    private boolean isPaused;
    private List<String> lines;
    private int currentLetter;
    private String currentLetterString;
    private float letterSpacing;
    private float gap, totalHeight, maxLineWidth;
    private final Map<Integer, Vector2f> letterOffsets = new HashMap<>();
    private final Map<Integer, Long> letterStartTime = new HashMap<>();
    private DialogLetterEffect dialogLetterEffect;
    private long lastTimeChar, animationTime, pauseStartTime, totalPauseTime;

    public DialogAnimationScrollText(String text, float letterSpacing, float gap, int maxWidth, Dialog dialog) {
        this.maxWidth = maxWidth;
        this.lines = splitText(text);
        this.letterSpacing = letterSpacing;
        this.gap = gap;
        this.currentLetter = 0;
        this.dialogLetterEffect = new DialogLetterEffect(DialogAnimationType.NONE, 0, 0, 0, text.length() - 1);
        long now = System.currentTimeMillis();
        for (int i = 0; i < text.length(); i++) {
            letterStartTime.put(i, now + i * 50L);
        }
        this.dialog = dialog;
        this.currentLetterString = "";
        init();
    }

    public void init() {
        Minecraft client = Minecraft.getInstance();
        maxLineWidth = 0;
        for (String line : lines) {
            float lineWidth = client.font.width(line) + (line.length() - 1) * letterSpacing;
            maxLineWidth = Math.max(maxLineWidth, lineWidth);
        }
        totalHeight = 0;
        for (String text : lines) {
            if(lines.size() > 1) {
                totalHeight += gap;
            } else {
                totalHeight += client.font.lineHeight;
            }
        }
        if(lines.size() > 1) {
            totalHeight -= gap - client.font.lineHeight;
        }
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        Minecraft client = Minecraft.getInstance();
        long now = System.currentTimeMillis();
        int totalLetters = lines.stream().mapToInt(String::length).sum();
        int shownLetters = 0;

        float currentY = -getTotalHeight() + dialog.getPaddingY() * 2 + 0.7f;

        int globalCharIndex = 0;

        for (int i = 0; i < lines.size(); i++) {
            String text = lines.get(i);
            int lineLength = text.length();
            int lineVisibleLetters = Math.max(0, Math.min(lineLength, currentLetter - shownLetters));
            shownLetters += lineLength;

            float textWidth = client.font.width(text) + letterSpacing * (lineLength - 1);
            float startX = textWidth == maxLineWidth ? -textWidth / 2.0F : -maxLineWidth / 2.0F;

            if (!isPaused) {
                if (dialogLetterEffect.getAnimation() == DialogAnimationType.SHAKING && now - animationTime >= dialogLetterEffect.getTime()) {
                    animationTime = now;
                    letterOffsets.clear();
                    for (int j = dialogLetterEffect.getStartIndex() - 1; j < dialogLetterEffect.getEndIndex(); j++) {
                        float offsetX = MathUtils.getRandomFloat(-dialogLetterEffect.getForce(), dialogLetterEffect.getForce());
                        float offsetY = MathUtils.getRandomFloat(-dialogLetterEffect.getForce(), dialogLetterEffect.getForce());
                        letterOffsets.put(j, new Vector2f(offsetX, offsetY));
                    }
                } else if (dialogLetterEffect.getAnimation() == DialogAnimationType.WAVING) {
                    letterOffsets.clear();
                    float waveSpacing = 0.2f;
                    double waveSpeed = (double) (now - totalPauseTime) / dialogLetterEffect.getTime();

                    for (int j = dialogLetterEffect.getStartIndex() - 1; j < dialogLetterEffect.getEndIndex(); j++) {
                        float offsetY = (float) (Math.sin(waveSpeed + j * waveSpacing) * dialogLetterEffect.getForce());
                        letterOffsets.put(j, new Vector2f(0, offsetY));
                    }
                }
            }

            for (int j = 0; j < lineVisibleLetters; j++) {
                Vector2f offset = letterOffsets.getOrDefault(globalCharIndex, new Vector2f(0, 0));
                String character = String.valueOf(text.charAt(j));
                currentLetterString = character;
                drawString(character, poseStack, bufferSource, startX + offset.x, currentY + offset.y);

                Style style = Style.EMPTY;
                FontSet fontset = ((FontFields) client.font).callGetFontSet(style.getFont());
                GlyphInfo glyph = fontset.getGlyphInfo(text.codePointAt(j), ((FontFields) client.font).getFilterFishyGlyphs());
                boolean bold = style.isBold();
                float letterWidth = glyph.getAdvance(bold);

                startX += letterWidth + letterSpacing;

                globalCharIndex++;
            }

            currentY += lines.size() > 1 ? gap : client.font.lineHeight;
        }

        if (currentLetter < totalLetters && now - lastTimeChar >= showLetterDelay && !client.isPaused()) {
            if(!currentLetterString.equals(" ") && !currentLetterString.isEmpty()) {
                ResourceLocation soundRes = ResourceLocation.withDefaultNamespace("custom.dialog_sound");
                SoundEvent sound = SoundEvent.createVariableRangeEvent(soundRes);
                float pitch = 0.8F + new Random().nextFloat() * 0.4F;
                client.player.playSound(sound, 1.0F, pitch);
            }
            currentLetter++;
            lastTimeChar = now;
        }

        dialog.getDialogEntityBobbing().updateLookDirection();

    }

    private void drawString(String character, PoseStack poseStack, MultiBufferSource bufferSource, float x, float y) {
        Minecraft client = Minecraft.getInstance();

        int color = dialog.getTextDialogColor();

        client.font.drawInBatch(
                character,
                x,
                y,
                color,
                false,
                poseStack.last().pose(),
                bufferSource,
                Font.DisplayMode.SEE_THROUGH,
                0,
                15728880
        );

    }

    private List<String> splitText(String text) {

        List<String> finalString = new ArrayList<>();
        Minecraft client = Minecraft.getInstance();
        List<FormattedCharSequence> charSequences = client.font.split(FormattedText.of(text), maxWidth);
        for(FormattedCharSequence chara : charSequences) {
            StringBuilder stringBuilder = new StringBuilder();
            chara.accept((i, style, i1) -> {
                stringBuilder.appendCodePoint(i1);
                return true;
            });
            finalString.add(stringBuilder.toString());
        }
        return finalString;
    }

    public boolean isFinished() {
        return currentLetter == lines.stream().mapToInt(String::length).sum();
    }

    public void forceFinish() {
        currentLetter = lines.stream().mapToInt(String::length).sum();
    }

    public float getMaxWidthLine() {
        return maxLineWidth;
    }

    public void setMaxLineWidth(float maxLineWidth) {
        this.maxLineWidth = maxLineWidth;
        dialog.setOldWidth(maxLineWidth);
    }

    public void reset() {
        currentLetter = 0;
        init();
    }

    public List<String> getLines() {
        return lines;
    }

    public void setText(String text) {
        this.lines = splitText(text);
    }

    public float getLetterSpacing() {
        return letterSpacing;
    }

    public void setLetterSpacing(float letterSpacing) {
        this.letterSpacing = letterSpacing;
        init();
    }

    public float getGap() {
        return gap;
    }

    public void setGap(float gap) {
        this.gap = gap;
        init();
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public float getTotalHeight() {
        return totalHeight + 4 * dialog.getPaddingY();
    }

    public void setTotalHeight(float totalHeight) {
        this.totalHeight = totalHeight;
        dialog.setOldHeight(totalHeight);
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public DialogLetterEffect getDialogLetterEffect() {
        return dialogLetterEffect;
    }

    public void setDialogLetterEffect(DialogLetterEffect dialogLetterEffect) {
        this.dialogLetterEffect = dialogLetterEffect;
    }

    public void applyEffect(DialogLetterEffect dialogEffect, int startIndex, int endIndex) {
    }
}
