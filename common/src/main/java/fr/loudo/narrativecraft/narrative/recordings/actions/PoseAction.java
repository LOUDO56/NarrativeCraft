package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

public class PoseAction extends Action {

    private final Pose pose;
    private final Pose previousPose;

    public PoseAction(int waitTick, Pose pose, Pose previousPose) {
        super(waitTick, ActionType.POSE);
        this.pose = pose;
        this.previousPose = previousPose;
    }

    @Override
    public void execute(Entity entity) {
        if(entity instanceof LivingEntity livingEntity){
            if(pose != Pose.SLEEPING) {
                livingEntity.clearSleepingPos();
            }
            livingEntity.setPose(pose);
        }
    }

    @Override
    public void rewind(Entity entity) {
        if(entity instanceof LivingEntity livingEntity){
            if(previousPose != null) {
                entity.setPose(previousPose);
                if(previousPose != Pose.SLEEPING) {
                    livingEntity.clearSleepingPos();
                }
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
