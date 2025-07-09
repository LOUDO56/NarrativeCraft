package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import net.minecraft.world.entity.Entity;

public abstract class Action {

    protected int tick;
    protected ActionType actionType;

    public Action(int tick, ActionType actionType) {
        this.tick = tick;
        this.actionType = actionType;
    }

    public int getTick() {
        return tick;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public abstract void execute(Playback.PlaybackData playbackData);
    public abstract void rewind(Playback.PlaybackData playbackData);
}
