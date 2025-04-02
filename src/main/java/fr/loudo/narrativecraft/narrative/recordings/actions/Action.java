package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;

public class Action {

    private int waitTick;
    private ActionType actionType;

    public Action(int waitTick, ActionType actionType) {
        this.waitTick = waitTick;
        this.actionType = actionType;
    }

    public int getWaitTick() {
        return waitTick;
    }

    public ActionType getActionType() {
        return actionType;
    }
}
