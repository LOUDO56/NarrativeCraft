package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import fr.loudo.narrativecraft.utils.FakePlayer;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class UseItemAction extends Action {

    private String handName;

    public UseItemAction(int tick, InteractionHand interactionHand) {
        super(tick, ActionType.USE_ITEM);
        handName = interactionHand.name();
    }

    @Override
    public void execute(LivingEntity entity) {
        if(entity instanceof FakePlayer fakePlayer) {
            ServerLevel serverLevel = Utils.getServerLevel();
            ItemStack itemStack = entity.getItemInHand(InteractionHand.valueOf(handName));
            itemStack.setCount(2);
            itemStack.getItem().use(serverLevel, fakePlayer, InteractionHand.valueOf(handName));
        }
    }

    @Override
    public void rewind(LivingEntity entity) {}
}
