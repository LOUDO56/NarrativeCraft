package fr.loudo.narrativecraft.narrative.recordings.actions.manager;

import fr.loudo.narrativecraft.mixin.fields.EntityFields;
import fr.loudo.narrativecraft.mixin.fields.LivingEntityFields;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.actions.*;
import fr.loudo.narrativecraft.narrative.recordings.actions.modsListeners.EmoteCraftListeners;
import fr.loudo.narrativecraft.narrative.recordings.actions.modsListeners.ModsListenerImpl;
import fr.loudo.narrativecraft.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.vehicle.AbstractBoat;
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

    private final ActionsData actionsData;
    private final Recording recording;
    private Pose poseState;

    private byte entityByteState;
    private byte livingEntityByteState;

    private byte abstractHorseEntityByteState;

    private int abstractBoatEntityBubbleState;
    private boolean abstractBoatEntityLeftPaddleState;
    private boolean abstractBoatEntityRightPaddleState;

    private final HashMap<EquipmentSlot, ItemStack> currentItemInEquipmentSlot;
    private List<ModsListenerImpl> modsListenerList;

    public ActionDifferenceListener(ActionsData actionsData, Recording recording) {
        this.actionsData = actionsData;
        this.currentItemInEquipmentSlot = new HashMap<>();
        this.recording = recording;
        abstractBoatEntityLeftPaddleState = false;
        abstractBoatEntityRightPaddleState = false;
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
        byte entityCurrentByte = actionsData.getEntity().getEntityData().get(EntityFields.getDATA_SHARED_FLAGS_ID());
        if(entityByteState != entityCurrentByte) {
            EntityByteAction entityByteAction = new EntityByteAction(recording.getTick(), entityCurrentByte, entityByteState);
            entityByteState = entityCurrentByte;
            actionsData.addAction(entityByteAction);
        }
    }

    private void livingEntityByteListener() {
        byte livingEntityCurrentByte = actionsData.getEntity().getEntityData().get(LivingEntityFields.getDATA_LIVING_ENTITY_FLAGS());
        if(livingEntityByteState != livingEntityCurrentByte) {
            LivingEntityByteAction livingEntityByteAction = new LivingEntityByteAction(recording.getTick(), livingEntityCurrentByte, livingEntityByteState);
            livingEntityByteState = livingEntityCurrentByte;
            actionsData.addAction(livingEntityByteAction);
        }
    }

    public void abstractHorseEntityByteListener(byte abstractHorseCurrentByte) {
        if(actionsData.getEntity() instanceof AbstractHorse) {
            if(abstractHorseEntityByteState != abstractHorseCurrentByte) {
                AbstractHorseByteAction action = new AbstractHorseByteAction(recording.getTick(), abstractHorseCurrentByte, abstractHorseEntityByteState);
                abstractHorseEntityByteState = abstractHorseCurrentByte;
                actionsData.addAction(action);
            }
        }
    }

    public void abstractBoatEntityBubbleListener(int abstractBoatCurrentBubble) {
        if(actionsData.getEntity() instanceof AbstractBoat) {
            if(abstractBoatEntityBubbleState != abstractBoatCurrentBubble) {
                AbstractBoatBubbleAction action = new AbstractBoatBubbleAction(recording.getTick(), abstractBoatCurrentBubble, abstractBoatEntityBubbleState);
                abstractBoatEntityBubbleState = abstractBoatCurrentBubble;
                actionsData.addAction(action);
            }
        }
    }

    public void abstractBoatEntityPaddleListener(boolean left, boolean right) {
        if(actionsData.getEntity() instanceof AbstractBoat) {
            if(abstractBoatEntityLeftPaddleState != left || abstractBoatEntityRightPaddleState != right) {
                AbstractBoatPaddleAction action = new AbstractBoatPaddleAction(
                        recording.getTick(),
                        left,
                        right,
                        abstractBoatEntityLeftPaddleState,
                        abstractBoatEntityRightPaddleState
                );
                abstractBoatEntityLeftPaddleState = left;
                abstractBoatEntityRightPaddleState = right;
                actionsData.addAction(action);
            }
        }
    }

    private void itemListener() {

        for(EquipmentSlot equipmentSlot : equipmentSlotList) {
            ItemStack itemFromSlot = currentItemInEquipmentSlot.get(equipmentSlot);
            ItemStack currentItemFromSlot = ((LivingEntity)actionsData.getEntity()).getItemBySlot(equipmentSlot);
            if(BuiltInRegistries.ITEM.getId(itemFromSlot.getItem()) != BuiltInRegistries.ITEM.getId(currentItemFromSlot.getItem())) {
                currentItemInEquipmentSlot.replace(equipmentSlot, currentItemFromSlot.copy());
                onItemChange(currentItemFromSlot, itemFromSlot, equipmentSlot, recording.getTick());
            }
        }
}

    private void onItemChange(ItemStack itemStack, ItemStack oldItemStack, EquipmentSlot equipmentSlot, int tick) {
        ItemChangeAction itemChangeAction = new ItemChangeAction(tick, equipmentSlot.name(), itemStack, oldItemStack, actionsData.getEntity().registryAccess());
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
