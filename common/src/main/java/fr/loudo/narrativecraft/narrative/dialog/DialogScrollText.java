package fr.loudo.narrativecraft.narrative.dialog;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.joml.Random;
import org.joml.Vector4f;

public class DialogScrollText {

    private final long showLetterDelay = 30L;

    private boolean isPaused;
    private String text;
    private int currentLetter;
    private float gap;

    private long lastTimeChar, pauseStartTime;

    public DialogScrollText(String text, float gap) {
        this.text = text;
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
        if(isPaused) {
            return;
        }
        int guiScale = client.options.guiScale().get() > 0 ? client.options.guiScale().get() : 1;
        if(currentLetter < text.length() && now - lastTimeChar >= showLetterDelay) {
            ResourceLocation resourceLocation = ResourceLocation.withDefaultNamespace("custom.dialog_sound");
            SoundEvent sound = SoundEvent.createVariableRangeEvent(resourceLocation);
            Random random = new Random();
            float randomPitch = 0.8F + random.nextFloat() * (1.2F - 0.8F);
            Minecraft.getInstance().player.playSound(sound, 1.0F, randomPitch);
            currentLetter++;
            lastTimeChar = System.currentTimeMillis();
        }
        float totalWidth = 0;
        for (int i = 0; i < text.length(); i++) {
            String character = String.valueOf(text.charAt(i));
            totalWidth += (client.font.width(character) * scale + gap * scale) / guiScale;
        }
        float startX = screenX - (totalWidth / 2);
        for (int i = 0; i < currentLetter; i++) {
            String character = String.valueOf(text.charAt(i));
            drawString(guiGraphics, character, startX, screenY, scale, posClip);
            startX += (client.font.width(character) * scale + gap * scale) / guiScale;
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
                (screenY / scale) - ((float) client.font.lineHeight / 2),
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

    public String getText() {
        return text;
    }
}
