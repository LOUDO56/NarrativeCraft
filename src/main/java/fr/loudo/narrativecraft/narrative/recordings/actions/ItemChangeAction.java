package fr.loudo.narrativecraft.narrative.recordings.actions;

import com.mojang.datafixers.util.Pair;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

//TODO: complete ItemChangeAction class
public class ItemChangeAction extends Action {

    private int itemId;

    public ItemChangeAction(int waitTick, ActionType actionType, int itemId) {
        super(waitTick, actionType);
        this.itemId = itemId;
    }

    @Override
    public void execute(LivingEntity entity) {
        if(entity instanceof ServerPlayer player) {
            Item item = Item.byId(itemId);
            ItemStack itemStack = new ItemStack(Holder.direct(item));
            entity.getServer().getPlayerList().broadcastAll(new ClientboundSetEquipmentPacket(
                    entity.getId(),
                    List.of(new Pair<>(EquipmentSlot.MAINHAND, itemStack))
                    ));
        }
    }

}
