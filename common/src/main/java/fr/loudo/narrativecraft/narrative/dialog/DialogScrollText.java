package fr.loudo.narrativecraft.narrative.dialog;

import com.mojang.blaze3d.font.GlyphInfo;
import fr.loudo.narrativecraft.mixin.fields.FontFields;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.joml.Random;
import org.joml.Vector4f;

public class DialogScrollText {

    private final long showLetterDelay = 30L;
    private final float maxWidth = 30F;

    private boolean isPaused;
    private String text;
    private int currentLetter;
    private float letterSpacing;
    private float gap;

    private long lastTimeChar, pauseStartTime;

    public DialogScrollText(String text, float letterSpacing, float gap) {
        this.text = text;
        this.letterSpacing = letterSpacing;
        this.gap = gap;
        this.currentLetter = 0;
    }

    public void show(GuiGraphics guiGraphics, Vector4f posClip, float screenX, float screenY, float scale) {
        Minecraft client = Minecraft.getInstance();
        long now = System.currentTimeMillis();
        if(client.isPaused() && !isPaused) {
            isPaused = true;
            pauseStartTime = now;
        } else if(!client.isPaused() && isPaused) {
            isPaused = false;
            lastTimeChar += now - pauseStartTime;
        }
        int guiScale = client.options.guiScale().get() > 0 ? client.options.guiScale().get() : 1;
        if(currentLetter < text.length() && now - lastTimeChar >= showLetterDelay && !isPaused) {
            ResourceLocation resourceLocation = ResourceLocation.withDefaultNamespace("custom.dialog_sound");
            SoundEvent sound = SoundEvent.createVariableRangeEvent(resourceLocation);
            Random random = new Random();
            float randomPitch = 0.8F + random.nextFloat() * (1.2F - 0.8F);
            Minecraft.getInstance().player.playSound(sound, 1.0F, randomPitch);
            currentLetter++;
            lastTimeChar = System.currentTimeMillis();
        }
        float textWidth = client.font.width(text);
        textWidth += letterSpacing * (text.length() - 1);

        float startX = screenX - ((textWidth * scale) / 2.0F) / guiScale;
        for (int i = 0; i < currentLetter; i++) {
            String character = String.valueOf(text.charAt(i));
            drawString(guiGraphics, character, startX, screenY, scale, posClip);
            Style style = Style.EMPTY;
            FontSet fontset = ((FontFields)client.font).callGetFontSet(style.getFont());
            GlyphInfo glyphinfo = fontset.getGlyphInfo(text.codePointAt(i), ((FontFields)client.font).getFilterFishyGlyphs());
            boolean flag = style.isBold();
            float letterWidth = glyphinfo.getAdvance(flag);
            startX += (letterWidth * scale + letterSpacing * scale) / guiScale;
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
                (screenX / scale) ,
                (screenY / scale),
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

    public void reset() {
        currentLetter = 0;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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
}
