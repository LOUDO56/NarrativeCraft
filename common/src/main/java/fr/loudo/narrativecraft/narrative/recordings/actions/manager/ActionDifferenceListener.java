package fr.loudo.narrativecraft.narrative.recordings.actions.manager;

import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.actions.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ActionDifferenceListener {

    private final List<EquipmentSlot> equipmentSlotList = Arrays.asList(
            EquipmentSlot.MAINHAND,
            EquipmentSlot.OFFHAND,
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.BODY,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET,
            EquipmentSlot.SADDLE
    );
    private final EntityDataAccessor<Byte> entityMaskByte = new EntityDataAccessor<>(0, EntityDataSerializers.BYTE);

    private Recording recording;
    private ServerPlayer player;
    private Pose poseState;
    private byte entityByteState;
    private HashMap<EquipmentSlot, ItemStack> currentItemInEquipmentSlot;

    public ActionDifferenceListener(Recording recording) {
        this.player = recording.getPlayer();
        this.recording = recording;
        this.currentItemInEquipmentSlot = new HashMap<>();
        initItemSlot();
    }

    private void initItemSlot() {
        for(EquipmentSlot equipmentSlot : equipmentSlotList) {
            ItemStack currentItemFromSlot = player.getItemBySlot(equipmentSlot);
            currentItemInEquipmentSlot.put(equipmentSlot, currentItemFromSlot);
        }
    }

    public void listenDifference() {

        int tick = recording.getTickAction();

        swingListener(tick);
        poseListener(tick);
        byteListener(tick);
        itemListener(tick);
        hurtListener(tick);

//        if(player.isUsingItem()) {
//            ItemUsedAction itemUsedAction = new ItemUsedAction(tick, ActionType.ITEM_USED, player.getUsedItemHand());
//            recording.getActionsData().addAction(itemUsedAction);
//        }
    }

    private void swingListener(int tick) {
        if(player.swinging) {
            SwingAction action = new SwingAction(tick, ActionType.SWING, player.swingingArm);
            recording.getActionsData().addAction(action);
        }
    }

    private void poseListener(int tick) {
        if(player.getPose() != poseState) {
            poseState = player.getPose();
            PoseAction action = new PoseAction(tick, ActionType.POSE, player.getPose());
            recording.getActionsData().addAction(action);
        }
    }

    private void byteListener(int tick) {
        byte currentByte = player.getEntityData().get(entityMaskByte);
        if(entityByteState != currentByte) {
            entityByteState = currentByte;
            EntityByteAction entityByteAction = new EntityByteAction(tick, ActionType.ENTITY_BYTE, currentByte);
            recording.getActionsData().addAction(entityByteAction);
        }
    }

    private void itemListener(int tick) {

        for(EquipmentSlot equipmentSlot : equipmentSlotList) {
            ItemStack itemFromSlot = currentItemInEquipmentSlot.get(equipmentSlot);
            ItemStack currentItemFromSlot = player.getItemBySlot(equipmentSlot);
            if(!itemFromSlot.equals(currentItemFromSlot)) {
                currentItemInEquipmentSlot.replace(equipmentSlot, currentItemFromSlot);
                onItemChange(currentItemFromSlot, equipmentSlot, tick);
            }
        }
    }

    private void onItemChange(ItemStack itemStack, EquipmentSlot equipmentSlot, int tick) {
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

    private void hurtListener(int tick) {
        if(player.hurtMarked) {
            HurtAction hurtAction = new HurtAction(tick, ActionType.HURT);
            recording.getActionsData().addAction(hurtAction);
        }
    }


}
