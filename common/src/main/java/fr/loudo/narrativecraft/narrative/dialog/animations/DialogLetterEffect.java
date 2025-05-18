package fr.loudo.narrativecraft.narrative.dialog.animations;

import fr.loudo.narrativecraft.narrative.dialog.DialogAnimationType;

public class DialogLetterEffect {
    private DialogAnimationType animation;
    private long time;
    private float force;
    private int startIndex, endIndex;

    public DialogLetterEffect(DialogAnimationType animation, long time, float force, int startIndex, int endIndex) {
        this.animation = animation;
        this.time = time;
        this.force = force;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public DialogLetterEffect(DialogAnimationType animation) {
        this.animation = animation;
    }

    public DialogAnimationType getAnimation() {
        return animation;
    }

    public long getTime() {
        return time;
    }

    public float getForce() {
        return force;
    }

    public void setAnimation(DialogAnimationType animation) {
        this.animation = animation;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setForce(float force) {
        this.force = force;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }
}
