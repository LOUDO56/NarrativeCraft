package fr.loudo.narrativecraft.narrative.recordings;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;
import fr.loudo.narrativecraft.narrative.recordings.actions.ActionsData;
import fr.loudo.narrativecraft.narrative.recordings.actions.GameModeAction;
import fr.loudo.narrativecraft.narrative.recordings.actions.RidingAction;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionDifferenceListener;
import fr.loudo.narrativecraft.narrative.recordings.actions.modsListeners.ModsListenerImpl;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.ProjectileItem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Recording {

    private final RecordingHandler recordingHandler = NarrativeCraftMod.getInstance().getRecordingHandler();
    private final AtomicInteger ids = new AtomicInteger();

    private final int id;
    private final List<RecordingData> recordingDataList;
    private RecordingData entityRecorderData;
    private List<Entity> trackedEntities;
    private boolean isRecording;
    private int tick;

    public Recording(LivingEntity entity) {
        tick = 0;
        this.entityRecorderData = new RecordingData(entity, this);
        entityRecorderData.savingTrack = true;
        entityRecorderData.actionsData.setEntityIdRecording(ids.incrementAndGet());
        this.recordingDataList = new ArrayList<>();
        trackedEntities = new ArrayList<>();
        this.isRecording = false;
        id = RecordingHandler.ids.incrementAndGet();
    }

    public void tick() {

        List<UUID> trackedUUIDs = trackedEntities.stream()
                .map(Entity::getUUID)
                .toList();

        List<Entity> nearbyEntities = entityRecorderData.entity.level()
                .getEntities(entityRecorderData.entity, entityRecorderData.entity.getBoundingBox().inflate(30));

        for (Entity entity : nearbyEntities) {
            if (!trackedUUIDs.contains(entity.getUUID())
                    && !(entity instanceof ProjectileItem)
                    && !(entity instanceof EyeOfEnder)
                    && !(entity instanceof ThrowableItemProjectile)
            ) {
                trackedEntities.add(entity);
                RecordingData recordingData = new RecordingData(entity, this);
                recordingDataList.add(recordingData);
                if(entity instanceof VehicleEntity || entity instanceof AbstractHorse || entity instanceof ItemEntity) {
                    trackEntity(entity, tick);
                }
            }
        }

        for(RecordingData recordingData : recordingDataList) {
            if(NarrativeCraftMod.getInstance().getPlaybackHandler().entityInPlayback(recordingData.entity)) {
                recordingData.savingTrack = false;
            }
            recordingData.actionsData.addMovement();
            recordingData.actionDifferenceListener.listenDifference();
        }
        if(tick == 0 && entityRecorderData.entity.getVehicle() != null) {
            ActionsData actionsData = getActionDataFromEntity(entityRecorderData.entity.getVehicle());
            if(actionsData != null) {
                RidingAction action = new RidingAction(0, actionsData.getEntityIdRecording());
                entityRecorderData.actionsData.addAction(action);
            }
        }
        tick++;
    }

    public boolean start() {
        if(isRecording) return false;
        recordingHandler.addRecording(this);
        tick = 0;
        ids.set(0);
        entityRecorderData = new RecordingData(entityRecorderData.entity, this);
        entityRecorderData.savingTrack = true;
        entityRecorderData.actionsData.setEntityIdRecording(ids.incrementAndGet());
        recordingDataList.clear();
        trackedEntities.clear();
        recordingDataList.add(entityRecorderData);
        isRecording = true;
        if(entityRecorderData.entity instanceof ServerPlayer player) {
            GameModeAction gameModeAction = new GameModeAction(0, player.gameMode.getGameModeForPlayer(), player.gameMode.getGameModeForPlayer());
            entityRecorderData.actionsData.addAction(gameModeAction);
        }
        return true;
    }

    public boolean stop() {
        if(!isRecording) return false;
        isRecording = false;
        PlayerSession playerSession = NarrativeCraftMod.getInstance().getPlayerSession();
        for(Subscene subscene : playerSession.getSubscenesPlaying()) {
            subscene.forceStop();
        }
        playerSession.getSubscenesPlaying().clear();
        for(RecordingData recordingData : recordingDataList) {
            if(!(recordingData.entity instanceof LivingEntity)) continue;
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
            recordingDataList.clear();
            recordingHandler.removeRecording(this);
            return true;
        }
        return false;
    }

    public ActionsData getActionDataFromEntity(Entity entity) {
        for(RecordingData recordingData : recordingDataList) {
            if(recordingData.entity.getUUID().equals(entity.getUUID())) {
                recordingData.setSavingTrack(true);
                return recordingData.actionsData;
            }
        }
        return null;
    }

    public RecordingData getRecordingDataFromEntity(Entity entity) {
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

    public Entity getEntity() {
        return entityRecorderData.entity;
    }

    public int getTick() {
        return tick;
    }

    public AtomicInteger getIds() {
        return ids;
    }

    public boolean trackEntity(Entity entity) {
        RecordingData recordingData = getRecordingDataFromEntity(entity);
        if(recordingData == null || recordingData.savingTrack) return false;
        recordingData.setSavingTrack(true);
        recordingData.actionsData.setSpawnTick(0);
        recordingData.actionsData.setEntityIdRecording(ids.incrementAndGet());
        return true;
    }

    public void trackEntity(Entity entity, int tickSpawn) {
       if(!trackEntity(entity)) return;
       RecordingData recordingData = getRecordingDataFromEntity(entity);
       recordingData.actionsData.setSpawnTick(tickSpawn);
    }

    public int getId() {
        return id;
    }

    public static class RecordingData {

        private final Entity entity;
        private final ActionDifferenceListener actionDifferenceListener;
        private final ActionsData actionsData;
        private boolean savingTrack;

        public RecordingData(Entity entity, Recording recording) {
            this.entity = entity;
            actionsData = new ActionsData(entity, recording.tick);
            actionDifferenceListener = new ActionDifferenceListener(actionsData, recording);
            savingTrack = false;
        }

        public Entity getEntity() {
            return entity;
        }

        public ActionDifferenceListener getActionDifferenceListener() {
            return actionDifferenceListener;
        }

        public ActionsData getActionsData() {
            return actionsData;
        }

        public boolean isSavingTrack() {
            return savingTrack;
        }

        public void setSavingTrack(boolean savingTrack) {
            this.savingTrack = savingTrack;
        }
    }
}
