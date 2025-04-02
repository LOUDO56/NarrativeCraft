package fr.loudo.narrativecraft.narrative.recordings.actions.manager;

import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.actions.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ActionDifference {

    private final EntityDataAccessor<Byte> entityMaskByte = new EntityDataAccessor<>(0, EntityDataSerializers.BYTE);

    private Recording recording;
    private ServerPlayer player;
    private Pose poseState;
    private byte entityByteState;
    private ItemStack itemStackState;

    public ActionDifference(Recording recording) {
        this.player = recording.getPlayer();
        this.recording = recording;
    }

    public void listenDifference(int tick) {

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

        ItemStack currentItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if(!currentItem.equals(itemStackState)) {
            itemStackState = currentItem;
            for(Object dataComponents : currentItem.getComponents()) {
                System.out.println(dataComponents.toString());
            }
            ItemChangeAction itemChangeAction = new ItemChangeAction(tick, ActionType.ITEM_CHANGE, Item.getId(currentItem.getItem()));
            recording.getActionsData().addAction(itemChangeAction);
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
}
