package fr.loudo.narrativecraft.narrative.story.inkAction;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.utils.MathUtils;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class FadeScreenInkAction extends InkAction {

    private double fadeIn, stay, fadeOut, t;
    private int color;
    private long startTime, pauseStartTime;
    private FadeCurrentState fadeCurrentState;

    public FadeScreenInkAction(StoryHandler storyHandler) {
        super(storyHandler);
    }

    @Override
    public boolean execute(String[] command) {
        fadeCurrentState = FadeCurrentState.FADE_IN;
        startTime = System.currentTimeMillis();
        fadeIn = 2.0;
        stay = 1.0;
        fadeOut = 2.0;
        color = 0x000000;
        t = 0;
        if(command.length >= 2) {
            fadeIn = Double.parseDouble(command[1]);
        }
        if(command.length >= 3) {
            stay = Double.parseDouble(command[2]);
        }
        if(command.length >= 4) {
            fadeOut = Double.parseDouble(command[3]);
        }
        if(command.length >= 5) {
            color = Integer.parseInt(command[4], 16);
        }
        storyHandler.setFadeScreenInkAction(this);
        sendDebugDetails();
        return true;
    }

    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        int width = Minecraft.getInstance().getWindow().getWidth();
        int height = Minecraft.getInstance().getWindow().getHeight();
        long now = System.currentTimeMillis();
        long elapsedTime = now - startTime;
        long endTime = 0L;
        int opacityInterpolate = 255;
        switch (fadeCurrentState) {
            case FADE_IN -> {
                endTime = (long) (fadeIn * 1000L);
                if(endTime > 0) {
                    opacityInterpolate = (int) MathUtils.lerp(0, 255, t);
                }
            }
            case STAY -> {
                endTime = (long) (stay * 1000L);
            }
            case FADE_OUT -> {
                endTime = (long) (fadeOut * 1000L);
                if(endTime > 0) {
                    opacityInterpolate = (int) MathUtils.lerp(255, 0, t);
                }
            }
        }
        int newColor = (opacityInterpolate << 24) | (color & 0xFFFFFF);
        guiGraphics.fill(0, 0, width, height, newColor);
        t = Math.min((double) elapsedTime / endTime, 1.0);
        if(t >= 1.0) {
            t = 0.0;
            startTime = System.currentTimeMillis();
            switch (fadeCurrentState) {
                case FADE_IN -> fadeCurrentState = FadeCurrentState.STAY;
                case STAY -> fadeCurrentState = FadeCurrentState.FADE_OUT;
                case FADE_OUT -> {
                    storyHandler.setFadeScreenInkAction(null);
                    if(!storyHandler.getStory().canContinue()) {
                        storyHandler.stop();
                        return;
                    }
                }
            }
            sendDebugDetails();
        }
    }

    @Override
    void sendDebugDetails() {
        double seconds = 0;
        switch (fadeCurrentState) {
            case FADE_IN -> seconds = fadeIn;
            case STAY -> seconds = stay;
            case FADE_OUT -> seconds = fadeOut;
        }
        if(storyHandler.isDebugMode()) {
            Minecraft.getInstance().player.displayClientMessage(Translation.message("debug.fade", fadeCurrentState.name(), seconds), false);
        }
    }

    enum FadeCurrentState {
        FADE_IN,
        STAY,
        FADE_OUT
    }

}
