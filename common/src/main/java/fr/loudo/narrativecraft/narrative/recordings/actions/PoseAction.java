package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

public class PoseAction extends Action {

    private Pose pose;
    private Pose previousPose;

    public PoseAction(int waitTick, Pose pose, Pose previousPose) {
        super(waitTick, ActionType.POSE);
        this.pose = pose;
        this.previousPose = previousPose;
    }

    @Override
    public void execute(LivingEntity entity) {
        if(pose != Pose.SLEEPING) {
            entity.clearSleepingPos();
        }
        entity.setPose(pose);
    }

    @Override
    public void rewind(LivingEntity entity) {
        if(previousPose != null) {
            entity.setPose(previousPose);
            if(previousPose != Pose.SLEEPING) {
                entity.clearSleepingPos();
            }
        }
    }

    public Pose getPreviousPose() {
        return previousPose;
    }

    public Pose getPose() {
        return pose;
    }
}
