package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

//TODO: complete ItemChangeAction class
public class ItemChangeAction extends Action {

    private Item item;

    public ItemChangeAction(int waitTick, ActionType actionType, ItemStack itemStack) {
        super(waitTick, actionType);
    }

    @Override
    public void execute(LivingEntity entity) {

    }

}
