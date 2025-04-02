package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

public class ItemUsedAction extends Action {

    private InteractionHand interactionHand;

    public ItemUsedAction(int tick, ActionType actionType, InteractionHand interactionHand) {
        super(tick, actionType);
        this.interactionHand = interactionHand;
    }

    @Override
    public void execute(LivingEntity entity) {
        entity.startUsingItem(interactionHand);
    }
}
