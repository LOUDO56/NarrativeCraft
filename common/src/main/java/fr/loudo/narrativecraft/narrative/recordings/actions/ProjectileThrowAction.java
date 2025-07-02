package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import net.minecraft.world.entity.LivingEntity;

public class ProjectileThrowAction extends Action {

    private int itemId;
    private String handName;

    // Standby, don't work.
    public ProjectileThrowAction(int tick, int itemId, String handName) {
        super(tick, ActionType.PROJECTILE_THROW);
        this.itemId = itemId;
        this.handName = handName;
    }

    @Override
    public void execute(LivingEntity entity) {}

    @Override
    public void rewind(LivingEntity entity) {}
}
