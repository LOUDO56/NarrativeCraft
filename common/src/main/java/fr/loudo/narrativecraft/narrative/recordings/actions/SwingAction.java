package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class SwingAction extends Action {

    private InteractionHand interactionHand;

    public SwingAction(int waitTick, InteractionHand interactionHand) {
        super(waitTick, ActionType.SWING);
        this.interactionHand = interactionHand;
    }

    @Override
    public void execute(Entity entity) {
        if(entity instanceof LivingEntity livingEntity) {
            livingEntity.swing(interactionHand);
        }
    }

    @Override
    public void rewind(Entity entity) {}
}
