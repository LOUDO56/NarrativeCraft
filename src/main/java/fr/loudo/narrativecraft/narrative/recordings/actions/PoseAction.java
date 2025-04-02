package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import net.minecraft.world.entity.Pose;

public class PoseAction extends Action {

    private Pose pose;

    public PoseAction(int waitTick, ActionType actionType, Pose pose) {
        super(waitTick, actionType);
        this.pose = pose;
    }

    public Pose getPose() {
        return pose;
    }
}
