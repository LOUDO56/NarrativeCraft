package fr.loudo.narrativecraft.narrative.recordings.actions.manager;

import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.actions.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class ActionDifference {

    private final EntityDataAccessor<Byte> entityMaskByte = new EntityDataAccessor<>(0, EntityDataSerializers.BYTE);

    private Recording recording;
    private ServerPlayer player;
    private Pose poseState;
    private byte entityByteState;
    private ItemStack itemStackStateMain;
    private ItemStack itemStackStateOff;

    public ActionDifference(Recording recording) {
        this.player = recording.getPlayer();
        this.recording = recording;
    }

    public void listenDifference() {

        int tick = recording.getTickAction();
        if(player.swinging) {
            SwingAction action = new SwingAction(tick, ActionType.SWING, player.swingingArm);
            recording.getActionsData().addAction(action);
        }

        if(player.getPose() != poseState) {
            poseState = player.getPose();
            PoseAction action = new PoseAction(tick, ActionType.POSE, player.getPose());
            recording.getActionsData().addAction(action);
        }

        byte currentByte = player.getEntityData().get(entityMaskByte);
        if(entityByteState != currentByte) {
            entityByteState = currentByte;
            EntityByteAction entityByteAction = new EntityByteAction(tick, ActionType.ENTITY_BYTE, currentByte);
            recording.getActionsData().addAction(entityByteAction);
        }

        ItemStack currentItemMain = player.getItemInHand(InteractionHand.MAIN_HAND);
        if(!currentItemMain.equals(itemStackStateMain)) {
            itemStackStateMain = currentItemMain;
            onItemChange(currentItemMain, EquipmentSlot.MAINHAND);
        }
        ItemStack currentItemOff = player.getItemInHand(InteractionHand.OFF_HAND);
        if(!currentItemOff.equals(itemStackStateOff)) {
            itemStackStateOff = currentItemOff;
            onItemChange(currentItemOff, EquipmentSlot.OFFHAND);
        }

        if(player.hurtMarked) {
            HurtAction hurtAction = new HurtAction(tick, ActionType.HURT);
            recording.getActionsData().addAction(hurtAction);
        }

//        if(player.isUsingItem()) {
//            ItemUsedAction itemUsedAction = new ItemUsedAction(tick, ActionType.ITEM_USED, player.getUsedItemHand());
//            recording.getActionsData().addAction(itemUsedAction);
//        }
    }

    private void onItemChange(ItemStack itemStack, EquipmentSlot equipmentSlot) {
        int tick = recording.getTickAction();
        if(itemStack.isEmpty()) {
            recording.getActionsData().addAction(new ItemChangeAction(tick, ActionType.ITEM_CHANGE, equipmentSlot.name(), Item.getId(itemStack.getItem())));
            return;
        }
        Tag tag = itemStack.save(player.registryAccess());
        Tag componentsTag = ((CompoundTag)tag).get("components");
        ItemChangeAction itemChangeAction;
        if(componentsTag == null) {
            itemChangeAction = new ItemChangeAction(tick, ActionType.ITEM_CHANGE, equipmentSlot.name(), Item.getId(itemStack.getItem()));
        } else {
            itemChangeAction = new ItemChangeAction(tick, ActionType.ITEM_CHANGE, Item.getId(itemStack.getItem()), equipmentSlot.name(), componentsTag.toString());
        }
        recording.getActionsData().addAction(itemChangeAction);
    }


}
