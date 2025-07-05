package fr.loudo.narrativecraft.narrative.recordings;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.recordings.actions.ActionsData;
import fr.loudo.narrativecraft.narrative.recordings.actions.GameModeAction;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionDifferenceListener;
import fr.loudo.narrativecraft.narrative.recordings.actions.modsListeners.ModsListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Recording {

    private final RecordingHandler recordingHandler = NarrativeCraftMod.getInstance().getRecordingHandler();

    private final RecordingData entityRecorderData;
    private final List<RecordingData> recordingDataList;
    private List<Entity> trackedEntities;
    private boolean isRecording;
    private int tick;

    public Recording(LivingEntity entity) {
        tick = 0;
        this.entityRecorderData = new RecordingData(entity, this);
        entityRecorderData.savingTrack = true;
        this.recordingDataList = new ArrayList<>();
        this.isRecording = false;
    }

    public void tick() {

        trackedEntities = entityRecorderData.entity.level().getEntities(entityRecorderData.entity, entityRecorderData.entity.getBoundingBox().inflate(30));

        Set<UUID> trackedUUIDs = trackedEntities.stream()
                .map(Entity::getUUID)
                .collect(Collectors.toSet());

        List<Entity> nearbyEntities = entityRecorderData.entity.level()
                .getEntities(entityRecorderData.entity, entityRecorderData.entity.getBoundingBox().inflate(30));

        for (Entity entity : nearbyEntities) {
            if (trackedUUIDs.contains(entity.getUUID()) && entity instanceof LivingEntity livingEntity) {
                recordingDataList.add(new RecordingData(livingEntity, this));
            }
        }

        for(RecordingData recordingData : recordingDataList) {
            recordingData.actionsData.addMovement();
            recordingData.actionDifferenceListener.listenDifference();
        }
        tick++;
    }

    public boolean start() {
        if(isRecording) return false;
        recordingDataList.clear();
        recordingDataList.add(entityRecorderData);
        recordingHandler.removeRecording(this);
        recordingHandler.addRecording(this);
        isRecording = true;
        if(entityRecorderData.entity instanceof ServerPlayer player) {
            GameModeAction gameModeAction = new GameModeAction(0, player.gameMode.getGameModeForPlayer(), player.gameMode.getGameModeForPlayer());
            entityRecorderData.actionsData.addAction(gameModeAction);
        }
        trackedEntities = entityRecorderData.entity.level().getEntities(entityRecorderData.entity, entityRecorderData.entity.getBoundingBox().inflate(30));
        for(Entity entity : trackedEntities) {
            if(entity instanceof LivingEntity livingEntity)  {
                recordingDataList.add(new RecordingData(livingEntity, this));
            }
        }
        return true;
    }

    public boolean stop() {
        if(!isRecording) return false;
        isRecording = false;
        for(RecordingData recordingData : recordingDataList) {
            recordingData.actionsData.reset(recordingData.entity);
            for(ModsListenerImpl modsListener : recordingData.actionDifferenceListener.getModsListenerList()) {
                modsListener.stop();
            }
        }
        return true;
    }

    public boolean save(Animation animation) {
        if(isRecording) return false;
        List<ActionsData> actionsDataList = recordingDataList.stream()
                .filter(recordingData -> recordingData.savingTrack)
                .map(recordingData -> recordingData.actionsData)
                .toList();
        animation.setActionsData(actionsDataList);
        if(NarrativeCraftFile.updateAnimationFile(animation)) {
            animation.getScene().addAnimation(animation);
            recordingHandler.removeRecording(this);
            recordingDataList.clear();
            return true;
        }
        return false;
    }

    public ActionsData getActionDataFromEntity(LivingEntity entity) {
        for(RecordingData recordingData : recordingDataList) {
            if(recordingData.entity.getUUID().equals(entity.getUUID())) {
                recordingData.savingTrack = true;
                return recordingData.actionsData;
            }
        }
        return null;
    }

    public RecordingData getRecordingDataFromEntity(LivingEntity entity) {
        for(RecordingData recordingData : recordingDataList) {
            if(recordingData.entity.getUUID().equals(entity.getUUID())) {
                return recordingData;
            }
        }
        return null;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public LivingEntity getEntity() {
        return entityRecorderData.entity;
    }

    public int getTick() {
        return tick;
    }

    public static class RecordingData {

        private final LivingEntity entity;
        private final ActionDifferenceListener actionDifferenceListener;
        private final ActionsData actionsData;
        private boolean savingTrack;

        public RecordingData(LivingEntity entity, Recording recording) {
            this.entity = entity;
            actionsData = new ActionsData(entity, recording.tick);
            actionDifferenceListener = new ActionDifferenceListener(actionsData, recording);
            savingTrack = false;
        }
    }
}
