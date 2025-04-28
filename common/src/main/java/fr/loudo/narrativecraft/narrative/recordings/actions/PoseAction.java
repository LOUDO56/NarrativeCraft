package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

public class PoseAction extends Action {

    private Pose pose;
    private Pose previousPose;

    public PoseAction(int waitTick, ActionType actionType, Pose pose, Pose previousPose) {
        super(waitTick, actionType);
        this.pose = pose;
        this.previousPose = previousPose;
    }

    @Override
    public void execute(LivingEntity entity) {
        entity.setPose(pose);
    }

    public void execute(LivingEntity entity, boolean previousOne) {
        if(previousPose != null) {
            entity.setPose(previousPose);
        }
    }

    public Pose getPreviousPose() {
        return previousPose;
    }
}
