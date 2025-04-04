package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

public class ItemUsedAction extends Action {

    private InteractionHand interactionHand;
    private int useItemRemainingTicks;

    public ItemUsedAction(int tick, ActionType actionType, InteractionHand interactionHand, int useItemRemainingTicks) {
        super(tick, actionType);
        this.interactionHand = interactionHand;
        this.useItemRemainingTicks = useItemRemainingTicks;
    }

    @Override
    public void execute(LivingEntity entity) {
        System.out.println("item used played");
        entity.startUsingItem(interactionHand);
    }
}
