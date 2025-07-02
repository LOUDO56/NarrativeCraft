package fr.loudo.narrativecraft.narrative.recordings.actions;

import com.mojang.datafixers.util.Pair;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;


public class ItemChangeAction extends Action {

    private int itemId;
    private String data;
    private String equipmentSlot;

    public ItemChangeAction(int waitTick, String equipmentSlot, int itemId) {
        super(waitTick, ActionType.ITEM_CHANGE);
        this.itemId = itemId;
        this.equipmentSlot = equipmentSlot;
        this.data = "";
    }

    public ItemChangeAction(int waitTick, int itemId, String equipmentSlot, String data) {
        super(waitTick, ActionType.ITEM_CHANGE);
        this.itemId = itemId;
        this.equipmentSlot = equipmentSlot;
        this.data = data;
    }

    @Override
    public void execute(LivingEntity entity) {
        Item item = BuiltInRegistries.ITEM.byId(itemId);
        ItemStack itemStack = new ItemStack(item);
        CompoundTag tag = Utils.tagFromIdAndComponents(item, data);
        if (tag != null) {
            itemStack = ItemStack.parse(entity.registryAccess(), tag).orElse(ItemStack.EMPTY);
        }

        entity.getServer().getPlayerList().broadcastAll(new ClientboundSetEquipmentPacket(
                entity.getId(),
                List.of(new Pair<>(EquipmentSlot.valueOf(equipmentSlot), itemStack))
        ));
        entity.setItemSlot(EquipmentSlot.valueOf(equipmentSlot), itemStack);
    }

    @Override
    public void rewind(LivingEntity entity) {}

}
