package fr.loudo.narrativecraft.narrative.recordings.actions;

import com.mojang.datafixers.util.Pair;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;


public class ItemChangeAction extends Action {

    private final int itemId;
    private final int oldItemId;
    private final String data;
    private final String oldData;
    private final String equipmentSlot;

    public ItemChangeAction(int waitTick, String equipmentSlot, ItemStack itemStack, ItemStack oldItemStack, RegistryAccess registryAccess) {
        super(waitTick, ActionType.ITEM_CHANGE);
        this.itemId = BuiltInRegistries.ITEM.getId(itemStack.getItem());
        this.oldItemId = BuiltInRegistries.ITEM.getId(oldItemStack.getItem());
        this.equipmentSlot = equipmentSlot;
        this.data = getItemComponents(itemStack, registryAccess);
        this.oldData = getItemComponents(oldItemStack, registryAccess);
    }

    private String getItemComponents(ItemStack itemStack, RegistryAccess registryAccess) {
        if(itemStack.isEmpty()) return null;
        Tag tag = Utils.getItemTag(itemStack, registryAccess);
        Tag componentsTag = ((CompoundTag)tag).get("components");
        if(componentsTag != null) {
            return componentsTag.toString();
        }
        return null;
    }

    @Override
    public void execute(Playback.PlaybackData playbackData) {
        if(playbackData.getEntity()  instanceof LivingEntity livingEntity) {
            changeItem(livingEntity, itemId, data);
        }
    }

    @Override
    public void rewind(Playback.PlaybackData playbackData) {
        if(playbackData.getEntity()  instanceof LivingEntity livingEntity) {
            changeItem(livingEntity, oldItemId, oldData);
        }
    }

    private void changeItem(LivingEntity entity, int itemId, String data) {
        Item item = BuiltInRegistries.ITEM.byId(itemId);
        ItemStack itemStack = new ItemStack(item);
        if(data != null) {
            CompoundTag tag = Utils.tagFromIdAndComponents(item, data);
            if (tag != null) {
                itemStack = Utils.generateItemStackFromNBT(tag, entity.registryAccess());
            }
        }
        entity.getServer().getPlayerList().broadcastAll(new ClientboundSetEquipmentPacket(
                entity.getId(),
                List.of(new Pair<>(EquipmentSlot.valueOf(equipmentSlot), itemStack))
        ));
        entity.setItemSlot(EquipmentSlot.valueOf(equipmentSlot), itemStack);
    }

}
