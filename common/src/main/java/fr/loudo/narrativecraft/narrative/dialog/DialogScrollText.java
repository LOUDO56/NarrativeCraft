package fr.loudo.narrativecraft.narrative.dialog;

import com.mojang.blaze3d.font.GlyphInfo;
import fr.loudo.narrativecraft.mixin.fields.FontFields;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Random;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class DialogScrollText {

    private final long showLetterDelay = 30L;
    private final float offset = 4.1f;

    private int maxWidth;
    private boolean isPaused;
    private List<String> lines;
    private int currentLetter;
    private float letterSpacing;
    private float gap, startY, totalHeight, screenX, endY, screenY, scale;

    private long lastTimeChar, pauseStartTime;

    public DialogScrollText(String text, float letterSpacing, float gap, int maxWidth) {
        this.lines = splitText(text);
        this.letterSpacing = letterSpacing;
        this.gap = gap;
        this.currentLetter = 0;
        this.maxWidth = maxWidth;
    }

    public void init(float screenX, float screenY, float paddingY, float scale) {
        Minecraft client = Minecraft.getInstance();
        this.screenX = screenX;
        this.screenY = screenY;
        this.scale = scale;
        float totalGap = (lines.size() - 1) * gap;
        startY = screenY - ScreenUtils.getPixelValue(offset + totalGap, scale) - ScreenUtils.getPixelValue(paddingY, scale);
        //startY = (offset + totalGap) - paddingY;
        endY = startY;
        totalHeight = 0;
        for (String text : lines) {
            if(lines.size() > 1) {
                totalHeight += gap;
                endY += ScreenUtils.getPixelValue(gap, scale);
            } else {
                totalHeight += client.font.lineHeight;
                endY += ScreenUtils.getPixelValue(client.font.lineHeight, scale);
            }
        }
        if(lines.size() > 1) {
            totalHeight -= gap - client.font.lineHeight;
            endY -= ScreenUtils.getPixelValue(gap - client.font.lineHeight, scale);
        }
    }

    public void show(GuiGraphics guiGraphics, Vector4f posClip) {
        Minecraft client = Minecraft.getInstance();
        long now = System.currentTimeMillis();
        int guiScale = client.options.guiScale().get() > 0 ? client.options.guiScale().get() : 1;

        if (client.isPaused() && !isPaused) {
            isPaused = true;
            pauseStartTime = now;
        } else if (!client.isPaused() && isPaused) {
            isPaused = false;
            lastTimeChar += now - pauseStartTime;
        }

        int totalLetters = lines.stream().mapToInt(String::length).sum();
        int shownLetters = 0;

        if (currentLetter < totalLetters && now - lastTimeChar >= showLetterDelay && !isPaused) {
            ResourceLocation soundRes = ResourceLocation.withDefaultNamespace("custom.dialog_sound");
            SoundEvent sound = SoundEvent.createVariableRangeEvent(soundRes);
            float pitch = 0.8F + new Random().nextFloat() * 0.4F;
            client.player.playSound(sound, 1.0F, pitch);

            currentLetter++;
            lastTimeChar = System.currentTimeMillis();
        }

        float currentY = startY;

        for (String text : lines) {
            int lineLength = text.length();
            int lineVisibleLetters = Math.max(0, Math.min(lineLength, currentLetter - shownLetters));
            shownLetters += lineLength;

            float textWidth = client.font.width(text) + letterSpacing * (lineLength - 1);
            float startX = screenX - ((textWidth * scale) / 2.0F) / guiScale;

            for (int i = 0; i < lineVisibleLetters; i++) {
                String character = String.valueOf(text.charAt(i));
                drawString(guiGraphics, character, startX, currentY, scale, posClip);

                Style style = Style.EMPTY;
                FontSet fontset = ((FontFields) client.font).callGetFontSet(style.getFont());
                GlyphInfo glyph = fontset.getGlyphInfo(text.codePointAt(i), ((FontFields) client.font).getFilterFishyGlyphs());
                boolean bold = style.isBold();
                float letterWidth = glyph.getAdvance(bold);

                startX += (letterWidth * scale + letterSpacing * scale) / guiScale;
            }

            // if only one line, then fill for one line, else apply gap.
            if(lines.size() > 1) {
                currentY += ScreenUtils.getPixelValue(gap, scale);
            } else {
                currentY += ScreenUtils.getPixelValue(client.font.lineHeight, scale);
            }
        }
    }


    private void drawString(GuiGraphics guiGraphics, String character, float screenX, float screenY, float scale, Vector4f posClip) {
        Minecraft client = Minecraft.getInstance();
        MultiBufferSource.BufferSource buffers = client.renderBuffers().bufferSource();

        if (posClip.w <= 0) return;

        guiGraphics.pose().pushPose();
        int guiScale = client.options.guiScale().get();
        if (guiScale == 0) guiScale = 1;
        scale /= guiScale;
        guiGraphics.pose().scale(scale, scale, 2.0f);

        int color = 0xFFFFFF;

        client.font.drawInBatch(
                character,
                (screenX / scale),
                (screenY / scale) - (client.font.lineHeight / 2.0F),
                color,
                false,
                guiGraphics.pose().last().pose(),
                buffers,
                Font.DisplayMode.NORMAL,
                0,
                15728880
        );


        guiGraphics.pose().popPose();
        buffers.endBatch();

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

    public void reset() {
        currentLetter = 0;
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
    }

    public float getGap() {
        return gap;
    }

    public void setGap(float gap) {
        this.gap = gap;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public float getTotalHeight() {
        return totalHeight;
    }

    public float getStartY() {
        return startY;
    }

    public float getEndY() {
        return endY;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }
}
