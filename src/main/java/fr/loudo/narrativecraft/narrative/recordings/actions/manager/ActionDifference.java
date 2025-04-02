package fr.loudo.narrativecraft.narrative.recordings.actions.manager;

import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.actions.EntityByteAction;
import fr.loudo.narrativecraft.narrative.recordings.actions.PoseAction;
import fr.loudo.narrativecraft.narrative.recordings.actions.SwingAction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;

public class ActionDifference {

    private final EntityDataAccessor<Byte> entityMaskByte = new EntityDataAccessor<>(0, EntityDataSerializers.BYTE);

    private Recording recording;
    private ServerPlayer player;
    private Pose poseState;
    private byte entityByteState;

    public ActionDifference(Recording recording) {
        this.player = recording.getPlayer();
        this.recording = recording;
    }

    public boolean listenDifference(int tick) {

        if(player.swinging) {
            SwingAction action = new SwingAction(tick, ActionType.SWING, player.swingingArm);
            recording.getActionsData().addAction(action);
            return true;
        }

        if(player.getPose() != poseState) {
            poseState = player.getPose();
            PoseAction action = new PoseAction(tick, ActionType.POSE, player.getPose());
            recording.getActionsData().addAction(action);
            return true;
        }

        byte currentByte = player.getEntityData().get(entityMaskByte);
        if(entityByteState != currentByte) {
            entityByteState = currentByte;
            EntityByteAction entityByteAction = new EntityByteAction(tick, ActionType.ENTITY_BYTE, currentByte);
            recording.getActionsData().addAction(entityByteAction);
            return true;
        }

        return false;

    }
}
