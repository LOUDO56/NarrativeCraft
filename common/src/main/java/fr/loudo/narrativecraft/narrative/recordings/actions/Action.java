package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import net.minecraft.world.entity.LivingEntity;

public abstract class Action {

    private int tick;
    private ActionType actionType;

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

    public abstract void execute(LivingEntity entity);
    public abstract void rewind(LivingEntity entity);
}
