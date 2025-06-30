package fr.loudo.narrativecraft.narrative.dialog;

import fr.loudo.narrativecraft.narrative.dialog.animations.DialogAnimationScrollText;

public abstract class DialogImpl {

    protected DialogAnimationScrollText dialogAnimationScrollText;
    protected boolean acceptNewDialog, unSkippable, endDialog, dialogEnded, dialogAutoSkipped;
    protected long forcedEndTime, startTimeEnded;

    public void endDialogAndDontSkip() {
        unSkippable = true;
        endDialog();
    }

    public boolean isUnSkippable() {
        return unSkippable;
    }

    public void setUnSkippable(boolean unSkippable) {
        this.unSkippable = unSkippable;
    }

    public long getForcedEndTime() {
        return forcedEndTime;
    }

    public void setForcedEndTime(long forcedEndTime) {
        this.forcedEndTime = forcedEndTime;
    }

    public DialogAnimationScrollText getDialogAnimationScrollText() {
        return dialogAnimationScrollText;
    }

    public long getStartTimeEnded() {
        return startTimeEnded;
    }

    public boolean isEndDialog() {
        return endDialog;
    }

    public boolean isDialogAutoSkipped() {
        return dialogAutoSkipped;
    }

    public abstract void reset();
    public abstract void endDialog();
    public abstract boolean isAnimating();
}
