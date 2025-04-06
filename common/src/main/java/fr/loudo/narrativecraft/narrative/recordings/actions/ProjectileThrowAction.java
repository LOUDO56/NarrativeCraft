package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import net.minecraft.world.entity.LivingEntity;

public class ProjectileThrowAction extends Action {

    private int itemId;
    private String handName;

    // Standby, don't work.
    public ProjectileThrowAction(int tick, ActionType actionType, int itemId, String handName) {
        super(tick, actionType);
        this.itemId = itemId;
        this.handName = handName;
    }

    @Override
    public void execute(LivingEntity entity) {}
}
