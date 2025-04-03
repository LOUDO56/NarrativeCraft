package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

public class PoseAction extends Action {

    private Pose pose;

    public PoseAction(int waitTick, ActionType actionType, Pose pose) {
        super(waitTick, actionType);
        this.pose = pose;
    }

    @Override
    public void execute(LivingEntity entity) {
        entity.setPose(pose);
    }
}
