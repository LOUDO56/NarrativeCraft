package fr.loudo.narrativecraft.narrative.story.inkAction;

import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.inkAction.InkActionResult;
import fr.loudo.narrativecraft.narrative.story.inkAction.enums.InkTagType;
import fr.loudo.narrativecraft.narrative.story.inkAction.validation.ErrorLine;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;

public class CooldownInkAction extends InkAction {

    private long startTime;
    private long secondsToWait, pauseStartTime;
    private boolean isPaused;
    private String unitTime;

    public CooldownInkAction(StoryHandler storyHandler, String command) {
        super(storyHandler, InkTagType.COOLDOWN, command);
    }

    @Override
    public InkActionResult execute() {
        if(command.length < 2) return InkActionResult.error(this.getClass(), Translation.message("validation.missing_wait_value").getString());
        double timeValue;
        try {
            timeValue = Double.parseDouble(command[1]);
        } catch (NumberFormatException e) {
            return InkActionResult.error(this.getClass(), Translation.message("validation.number", command[1]).getString());
        }
        unitTime = command[2];
        if (unitTime.contains("second")) {
            secondsToWait = (long) (timeValue * 1000);
        } else if (unitTime.contains("minute")) {
            secondsToWait = (long) (timeValue * 60 * 1000);
        } else if (unitTime.contains("hour")) {
            secondsToWait = (long) (timeValue * 60 * 60 * 1000);
        } else {
            return InkActionResult.error(this.getClass(), Translation.message("validation.wrong_unit").getString());
        }

        startTime = System.currentTimeMillis();
        storyHandler.getInkActionList().add(this);
        if(storyHandler.getCurrentDialogBox() != null) {
            storyHandler.getCurrentDialogBox().endDialogAndDontSkip();
        }
        sendDebugDetails();
        return InkActionResult.block();
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
                    lineText, false
            );
        }
        String unitTime = command[2];
        if(!unitTime.contains("second") && !unitTime.contains("minute") && !unitTime.contains("hour")) {
            return new ErrorLine(
                    line,
                    scene,
                    Translation.message("validation.wrong_unit").getString(),
                    lineText, false
            );
        }
        try {
            Double.parseDouble(command[1]);
        } catch (NumberFormatException e) {
            return new ErrorLine(
                    line,
                    scene,
                    Translation.message("validation.number", unitTime.toUpperCase()).getString(),
                    lineText, false
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
