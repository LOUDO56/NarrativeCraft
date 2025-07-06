package fr.loudo.narrativecraft.narrative.recordings.actions.manager;

import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.actions.*;
import fr.loudo.narrativecraft.narrative.recordings.actions.modsListeners.EmoteCraftListeners;
import fr.loudo.narrativecraft.narrative.recordings.actions.modsListeners.ModsListenerImpl;
import fr.loudo.narrativecraft.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
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

    private final ActionsData actionsData;
    private final Recording recording;
    private Pose poseState;
    private byte entityByteState;
    private byte livingEntityByteState;
    private final HashMap<EquipmentSlot, ItemStack> currentItemInEquipmentSlot;
    private List<ModsListenerImpl> modsListenerList;

    public ActionDifferenceListener(ActionsData actionsData, Recording recording) {
        this.actionsData = actionsData;
        this.currentItemInEquipmentSlot = new HashMap<>();
        this.recording = recording;
        initItemSlot();
        initModsListeners();
    }

    private void initItemSlot() {
        for(EquipmentSlot equipmentSlot : equipmentSlotList) {
            currentItemInEquipmentSlot.put(equipmentSlot, new ItemStack(Items.AIR));
        }
    }

    private void initModsListeners() {
        modsListenerList = new ArrayList<>();
        if(Services.PLATFORM.isModLoaded("emotecraft")) {
            EmoteCraftListeners emoteCraftListeners = new EmoteCraftListeners(this);
            emoteCraftListeners.start();
            modsListenerList.add(emoteCraftListeners);
        }
    }

    public void listenDifference() {

        if(actionsData.getEntity() instanceof LivingEntity) {
            poseListener();
            entityByteListener();
            livingEntityByteListener();
            itemListener();
        }

    }

    private void poseListener() {
        if(actionsData.getEntity().getPose() != poseState) {
            PoseAction action = new PoseAction(recording.getTick(), actionsData.getEntity().getPose(), poseState);
            poseState = actionsData.getEntity().getPose();
            actionsData.addAction(action);
        }
    }

    private void entityByteListener() {
        byte entityCurrentByte = actionsData.getEntity().getEntityData().get(entityFlagByte);
        if(entityByteState != entityCurrentByte) {
            EntityByteAction entityByteAction = new EntityByteAction(recording.getTick(), entityCurrentByte, entityByteState);
            entityByteState = entityCurrentByte;
            actionsData.addAction(entityByteAction);
        }
    }

    private void livingEntityByteListener() {
        byte livingEntityCurrentByte = actionsData.getEntity().getEntityData().get(livingEntityFlagByte);
        if(livingEntityByteState != livingEntityCurrentByte) {
            livingEntityByteState = livingEntityCurrentByte;
            LivingEntityByteAction livingEntityByteAction = new LivingEntityByteAction(recording.getTick(), livingEntityCurrentByte);
            actionsData.addAction(livingEntityByteAction);
        }
    }

    private void itemListener() {

        for(EquipmentSlot equipmentSlot : equipmentSlotList) {
            ItemStack itemFromSlot = currentItemInEquipmentSlot.get(equipmentSlot);
            ItemStack currentItemFromSlot = ((LivingEntity)actionsData.getEntity()).getItemBySlot(equipmentSlot);
            if(Item.getId(itemFromSlot.getItem()) != Item.getId(currentItemFromSlot.getItem())) {
                currentItemInEquipmentSlot.replace(equipmentSlot, currentItemFromSlot.copy());
                onItemChange(currentItemFromSlot, equipmentSlot, recording.getTick());
            }
        }
}

    private void onItemChange(ItemStack itemStack, EquipmentSlot equipmentSlot, int tick) {
        if(itemStack.isEmpty()) {
            actionsData.addAction(new ItemChangeAction(tick, equipmentSlot.name(), BuiltInRegistries.ITEM.getId(itemStack.getItem())));
            return;
        }
        Tag tag = itemStack.save(actionsData.getEntity().registryAccess());
        Tag componentsTag = ((CompoundTag)tag).get("components");
        ItemChangeAction itemChangeAction;
        if(componentsTag == null) {
            itemChangeAction = new ItemChangeAction(tick, equipmentSlot.name(), Item.getId(itemStack.getItem()));
        } else {
            itemChangeAction = new ItemChangeAction(tick, BuiltInRegistries.ITEM.getId(itemStack.getItem()), equipmentSlot.name(), componentsTag.toString());
        }
        actionsData.addAction(itemChangeAction);
    }

    public Recording getRecording() {
        return recording;
    }

    public List<ModsListenerImpl> getModsListenerList() {
        return modsListenerList;
    }

    public ActionsData getActionsData() {
        return actionsData;
    }
}
