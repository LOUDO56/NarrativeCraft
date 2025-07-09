package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.MovementData;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.utils.FakePlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

public class DeathAction extends Action {

    private final int entityRecordingId;

    public DeathAction(int tick, int entityRecordingId) {
        super(tick, ActionType.DEATH);
        this.entityRecordingId = entityRecordingId;
    }

    @Override
    public void execute(Playback.PlaybackData playbackData) {
        if(playbackData.getActionsData().getEntityIdRecording() == entityRecordingId) {
            if(playbackData.getEntity() != null && playbackData.getEntity() instanceof LivingEntity livingEntity) {
                if(livingEntity instanceof FakePlayer) {
                    livingEntity.setHealth(0.0F);
                    livingEntity.level().broadcastEntityEvent(livingEntity, (byte)60);
                } else {
                    livingEntity.handleEntityEvent((byte)3);
                }
            }
        }
    }

    @Override
    public void rewind(Playback.PlaybackData playbackData) {
        Playback playback = playbackData.getPlayback();
        ActionsData actionsData = playbackData.getPlayback().getMasterEntityData();
        MovementData posToSpawn = actionsData.getMovementData().get(playback.getTick() - 1);
        if(posToSpawn == null) return;
        playback.respawnMasterEntity(posToSpawn);
    }

}
