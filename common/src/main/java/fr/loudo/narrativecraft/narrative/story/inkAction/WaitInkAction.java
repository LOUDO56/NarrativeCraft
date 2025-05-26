package fr.loudo.narrativecraft.narrative.story.inkAction;

import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;

public class WaitInkAction extends InkAction {

    private long startTime;
    private long secondsToWait, pauseStartTime;
    private boolean isPaused;
    private String unitTime;

    public WaitInkAction(StoryHandler storyHandler) {
        super(storyHandler);
    }

    @Override
    public boolean execute(String[] command) {
        long timeValue = Long.parseLong(command[1]);
        unitTime = command[2];
        if(unitTime.contains("second")) {
            secondsToWait = timeValue * 1000L;
        } else if(unitTime.contains("minute")) {
            secondsToWait = timeValue * 60 * 1000L;
        } else if (unitTime.contains("hour")) {
            secondsToWait = timeValue * 60 * 60 * 1000L;
        }
        startTime = System.currentTimeMillis();
        storyHandler.getInkActionList().add(this);
        if(storyHandler.getCurrentDialogBox() != null) {
            storyHandler.getCurrentDialogBox().endDialogAndDontSkip();
        }
        sendDebugDetails();
        return false;
    }

    @Override
    void sendDebugDetails() {
        if(storyHandler.isDebugMode()) {
            Minecraft.getInstance().player.displayClientMessage(Translation.message("debug.wait", secondsToWait / 1000L, unitTime), false);
        }
    }

    public long getStartTime() {
        return startTime;
    }

    public long getSecondsToWait() {
        return secondsToWait;
    }

    public void setSecondsToWait(long secondsToWait) {
        this.secondsToWait = secondsToWait;
    }

    public long getPauseStartTime() {
        return pauseStartTime;
    }

    public void setPauseStartTime(long pauseStartTime) {
        this.pauseStartTime = pauseStartTime;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }
}
