package fr.loudo.narrativecraft.narrative.story.inkAction;

import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
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
    private boolean isPaused, isDoneFading;
    private StoryHandler.FadeCurrentState fadeCurrentState;

    public FadeScreenInkAction() {}

    public FadeScreenInkAction(StoryHandler storyHandler) {
        super(storyHandler);
        isDoneFading = false;
    }

    @Override
    public InkActionResult execute(String[] command) {
        fadeCurrentState = StoryHandler.FadeCurrentState.FADE_IN;
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
        storyHandler.getInkActionList().add(this);
        sendDebugDetails();
        return InkActionResult.PASS;
    }

    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
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
        if(minecraft.isPaused() && !isPaused) {
            isPaused = true;
            pauseStartTime = now;
        } else if(!minecraft.isPaused() && isPaused) {
            isPaused = false;
            startTime += now - pauseStartTime;
        }
        int newColor = (opacityInterpolate << 24) | (color & 0xFFFFFF);
        guiGraphics.fill(0, 0, width, height, newColor);
        if(!isPaused) {
            t = Math.min((double) elapsedTime / endTime, 1.0);
            if(t >= 1.0) {
                t = 0.0;
                startTime = System.currentTimeMillis();
                switch (fadeCurrentState) {
                    case FADE_IN -> fadeCurrentState = StoryHandler.FadeCurrentState.STAY;
                    case STAY -> fadeCurrentState = StoryHandler.FadeCurrentState.FADE_OUT;
                    case FADE_OUT -> {
                        isDoneFading = true;
                        return;
                    }
                }
                sendDebugDetails();
            }
        }
    }

    public boolean isDoneFading() {
        return isDoneFading;
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

    @Override
    public ErrorLine validate(String[] command, int line, String lineText, Scene scene) {
        if(command.length >= 2) {
            try {
                Double.parseDouble(command[1]);
            } catch (NumberFormatException e) {
                return new ErrorLine(
                        line,
                        scene,
                        Translation.message("validation.number", StoryHandler.FadeCurrentState.FADE_OUT.name()).getString(),
                        lineText
                );
            }
        }
        if(command.length >= 3) {
            try {
                Double.parseDouble(command[2]);
            } catch (NumberFormatException e) {
                return new ErrorLine(
                        line,
                        scene,
                        Translation.message("validation.number", StoryHandler.FadeCurrentState.STAY.name()).getString(),
                        lineText
                );
            }
        }
        if(command.length >= 4) {
            try {
                Double.parseDouble(command[3]);
            } catch (NumberFormatException e) {
                return new ErrorLine(
                        line,
                        scene,
                        Translation.message("validation.number", StoryHandler.FadeCurrentState.FADE_OUT.name()).getString(),
                        lineText
                );
            }
        }
        if(command.length >= 5) {
            try {
                Integer.parseInt(command[4], 16);
            } catch (NumberFormatException e) {
                return new ErrorLine(
                        line,
                        scene,
                        Translation.message("validation.number", Translation.message("global.color")).getString(),
                        lineText
                );
            }
        }
        return null;
    }

}
