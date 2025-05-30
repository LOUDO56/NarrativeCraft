package fr.loudo.narrativecraft.narrative.recordings.actions.manager;

import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.actions.EntityByteAction;
import fr.loudo.narrativecraft.narrative.recordings.actions.ItemChangeAction;
import fr.loudo.narrativecraft.narrative.recordings.actions.LivingEntityByteAction;
import fr.loudo.narrativecraft.narrative.recordings.actions.PoseAction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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
    private final EntityDataAccessor<Byte> entityFlagByte = new EntityDataAccessor<>(0, EntityDataSerializers.BYTE);
    private final EntityDataAccessor<Byte> livingEntityFlagByte = new EntityDataAccessor<>(8, EntityDataSerializers.BYTE);

    private int tick;
    private Recording recording;
    private ServerPlayer player;
    private Pose poseState;
    private byte entityByteState;
    private byte livingEntityByteState;
    private HashMap<EquipmentSlot, ItemStack> currentItemInEquipmentSlot;

    public ActionDifferenceListener(Recording recording) {
        this.player = recording.getPlayer();
        this.recording = recording;
        this.currentItemInEquipmentSlot = new HashMap<>();
        this.tick = 0;
        initItemSlot();
    }

    private void initItemSlot() {
        for(EquipmentSlot equipmentSlot : equipmentSlotList) {
            currentItemInEquipmentSlot.put(equipmentSlot, new ItemStack(Items.AIR));
        }
    }

    public void listenDifference() {

        poseListener();
        entityByteListener();
        livingEntityByteListener();
        itemListener();

        tick++;

    }

    private void poseListener() {
        if(player.getPose() != poseState) {
            PoseAction action = new PoseAction(tick, ActionType.POSE, player.getPose(), poseState);
            poseState = player.getPose();
            recording.getActionsData().addAction(action);
        }
    }

    private void entityByteListener() {
        byte entityCurrentByte = player.getEntityData().get(entityFlagByte);
        if(entityByteState != entityCurrentByte) {
            EntityByteAction entityByteAction = new EntityByteAction(tick, ActionType.ENTITY_BYTE, entityCurrentByte, entityByteState);
            entityByteState = entityCurrentByte;
            recording.getActionsData().addAction(entityByteAction);
        }
    }

    private void livingEntityByteListener() {
        byte livingEntityCurrentByte = player.getEntityData().get(livingEntityFlagByte);
        if(livingEntityByteState != livingEntityCurrentByte) {
            livingEntityByteState = livingEntityCurrentByte;
            LivingEntityByteAction livingEntityByteAction = new LivingEntityByteAction(tick, ActionType.LIVING_ENTITY_BYTE, livingEntityCurrentByte);
            recording.getActionsData().addAction(livingEntityByteAction);
        }
    }

    private void itemListener() {

        for(EquipmentSlot equipmentSlot : equipmentSlotList) {
            ItemStack itemFromSlot = currentItemInEquipmentSlot.get(equipmentSlot);
            ItemStack currentItemFromSlot = player.getItemBySlot(equipmentSlot);
            if(Item.getId(itemFromSlot.getItem()) != Item.getId(currentItemFromSlot.getItem())) {
                currentItemInEquipmentSlot.replace(equipmentSlot, currentItemFromSlot.copy());
                onItemChange(currentItemFromSlot, equipmentSlot, tick);
            }
        }
    }

    private void onItemChange(ItemStack itemStack, EquipmentSlot equipmentSlot, int tick) {
        if(itemStack.isEmpty()) {
            recording.getActionsData().addAction(new ItemChangeAction(tick, ActionType.ITEM_CHANGE, equipmentSlot.name(), BuiltInRegistries.ITEM.getId(itemStack.getItem())));
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

    public int getTick() {
        return tick;
    }
}
