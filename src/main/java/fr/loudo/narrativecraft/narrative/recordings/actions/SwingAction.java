package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import net.minecraft.world.InteractionHand;

public class SwingAction extends Action {

    private InteractionHand interactionHand;

    public SwingAction(int waitTick, ActionType actionType, InteractionHand interactionHand) {
        super(waitTick, actionType);
        this.interactionHand = interactionHand;
    }

    public InteractionHand getInteractionHand() {
        return interactionHand;
    }
}
