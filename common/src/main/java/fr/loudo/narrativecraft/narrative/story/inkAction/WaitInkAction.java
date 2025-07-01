package fr.loudo.narrativecraft.narrative.story.inkAction;

import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;

public class WaitInkAction extends InkAction {

    private long startTime;
    private long secondsToWait, pauseStartTime;
    private boolean isPaused;
    private String unitTime;

    public WaitInkAction() {}

    public WaitInkAction(StoryHandler storyHandler) {
        super(storyHandler, InkTagType.WAIT);
    }

    @Override
    public InkActionResult execute(String[] command) {
        double timeValue = Double.parseDouble(command[1]);
        unitTime = command[2];
        if(unitTime.contains("second")) {
            secondsToWait = (long) timeValue * 1000L;
        } else if(unitTime.contains("minute")) {
            secondsToWait = (long) timeValue * 60 * 1000L;
        } else if (unitTime.contains("hour")) {
            secondsToWait = (long) timeValue * 60 * 60 * 1000L;
        }
        startTime = System.currentTimeMillis();
        storyHandler.getInkActionList().add(this);
        if(storyHandler.getCurrentDialogBox() != null) {
            storyHandler.getCurrentDialogBox().endDialogAndDontSkip();
        }
        sendDebugDetails();
        return InkActionResult.BLOCK;
    }

    public void checkForPause() {
        Minecraft client = Minecraft.getInstance();
        long now = System.currentTimeMillis();
        if(client.isPaused() && !isPaused) {
            isPaused = true;
            pauseStartTime = now;
        } else if (!client.isPaused() && isPaused) {
            long pauseTime = now - pauseStartTime;
            secondsToWait += pauseTime;
            isPaused = false;
        }
    }

    @Override
    void sendDebugDetails() {
        if(storyHandler.isDebugMode()) {
            Minecraft.getInstance().player.displayClientMessage(Translation.message("debug.wait", secondsToWait / 1000L, unitTime), false);
        }
    }

    @Override
    public ErrorLine validate(String[] command, int line, String lineText, Scene scene) {
        if(command.length < 2) {
            return new ErrorLine(
                    line,
                    scene,
                    Translation.message("validation.missing_wait_value").getString(),
                    lineText
            );
        }
        String unitTime = command[2];
        if(!unitTime.contains("second") && !unitTime.contains("minute") && !unitTime.contains("hour")) {
            return new ErrorLine(
                    line,
                    scene,
                    Translation.message("validation.wrong_unit").getString(),
                    lineText
            );
        }
        try {
            Double.parseDouble(command[1]);
        } catch (NumberFormatException e) {
            return new ErrorLine(
                    line,
                    scene,
                    Translation.message("validation.number", unitTime.toUpperCase()).getString(),
                    lineText
            );
        }
        return null;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getSecondsToWait() {
        return secondsToWait;
    }

    public boolean isPaused() {
        return isPaused;
    }

}
