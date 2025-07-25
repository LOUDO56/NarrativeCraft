package fr.loudo.narrativecraft.narrative.recordings.playback;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.mixin.fields.LivingEntityFields;
import fr.loudo.narrativecraft.mixin.fields.PlayerFields;
import fr.loudo.narrativecraft.mixin.fields.PlayerListFields;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.recordings.MovementData;
import fr.loudo.narrativecraft.narrative.recordings.actions.*;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.utils.FakePlayer;
import fr.loudo.narrativecraft.utils.MovementUtils;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Playback {

    private int id;
    private boolean isLooping;
    private Animation animation;
    private CharacterStory character;
    private LivingEntity masterEntity;
    private ServerLevel serverLevel;
    private PlaybackType playbackType;
    private Entity lastRideEntity;
    private boolean isPlaying, hasEnded, isUnique;
    private int globalTick;
    private final List<PlaybackData> entityPlaybacks = new ArrayList<>();

    public Playback(Animation animation, ServerLevel serverLevel, CharacterStory character, PlaybackType playbackType, boolean isLooping) {
        this.animation = animation;
        this.serverLevel = serverLevel;
        this.playbackType = playbackType;
        this.character = character;
        this.isPlaying = false;
        this.hasEnded = false;
        this.isLooping = isLooping;
        this.isUnique = false;
        id = PlaybackHandler.ids.incrementAndGet();
    }

    public boolean start() {

        if(playbackType == PlaybackType.PRODUCTION) {
            StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
            if(storyHandler.characterInStory(character)) {
                storyHandler.removeCharacter(character);
            }
        }

        globalTick = 0;
        isPlaying = true;
        hasEnded = false;
        entityPlaybacks.clear();

        ActionsData masterEntityData = animation.getActionsData().getFirst();
        MovementData firstLoc = masterEntityData.getMovementData().getFirst();
        PlaybackData playbackData = new PlaybackData(masterEntityData, this);
        playbackData.setEntity(masterEntity);
        entityPlaybacks.add(playbackData);
        if(masterEntity == null) {
            spawnMasterEntity(firstLoc);
        }

        for (int i = 1; i < animation.getActionsData().size(); i++) {
            ActionsData actionsData = animation.getActionsData().get(i);
            PlaybackData playbackData1 = new PlaybackData(actionsData, this);
            if(actionsData.getSpawnTick() == 0) {
                playbackData1.spawnEntity(actionsData.getMovementData().getFirst());
            }
            entityPlaybacks.add(playbackData1);
        }

        NarrativeCraftMod.getInstance().getPlaybackHandler().addPlayback(this);

        if (playbackType == PlaybackType.DEVELOPMENT) {
            NarrativeCraftMod.getInstance().getCharacterManager().reloadSkin(character);
        }
        for(PlaybackData playbackData1 : entityPlaybacks) {
            actionListener(playbackData1);
        }
        return true;
    }

    public void next() {
        for (PlaybackData playbackData : entityPlaybacks) {
            playbackData.tick(globalTick);
        }

        globalTick++;

        boolean allEnded = entityPlaybacks.stream().allMatch(PlaybackData::hasEnded);
        if (allEnded) {
            if (playbackType == PlaybackType.DEVELOPMENT) {
                PlayerSession playerSession = NarrativeCraftMod.getInstance().getPlayerSession();
                if (!(playerSession.getKeyframeControllerBase() instanceof CutsceneController)) {
                    finalizePlaybackCycle();
                    return;
                }
            } else {
                finalizePlaybackCycle();
            }
        }
        for(PlaybackData playbackData : entityPlaybacks) {
            actionListener(playbackData);
        }
    }

    public void finalizePlaybackCycle() {
        reset();
        for (PlaybackData playbackData : entityPlaybacks) {
            if (playbackData.getEntity() == null) continue;

            playbackData.actionsData.reset(playbackData.entity);
            if(isUnique) {
                playbackData.killEntity();
                stop();
                continue;
            }
            ActionsData actionsData = playbackData.getActionsData();
            if(isLooping) {
                List<MovementData> movementData = actionsData.getMovementData();
                if (movementData.isEmpty()) continue;
                if(movementData.getFirst().getVec3().distanceTo(movementData.getLast().getVec3()) >= 0.8) {
                    if(playbackData.entity.equals(masterEntity)) {
                        playbackData.killEntity();
                        spawnMasterEntity(movementData.getFirst());
                    } else {
                        playbackData.killEntity();
                        playbackData.spawnEntity(movementData.getFirst());
                    }
                }
            }
        }
        if(!isLooping) {
            stop();
        }
    }

    private void reset() {
        for(PlaybackData playbackData : entityPlaybacks) {
            playbackData.reset();
        }
        globalTick = 0;
    }

    public void killMasterEntity() {
        PlaybackData playbackData = entityPlaybacks.getFirst();
        masterEntity.remove(Entity.RemovalReason.KILLED);
        if(masterEntity instanceof FakePlayer fakePlayer) {
            NarrativeCraftMod.server.getPlayerList().remove(fakePlayer);
            ((PlayerListFields)serverLevel.getServer().getPlayerList()).getPlayersByUUID().remove(fakePlayer.getUUID());
        }
        playbackData.setEntity(null);
    }

    public void respawnMasterEntity(MovementData position) {
        spawnMasterEntity(position);
    }

    public void changeLocationByTick(int newTick, boolean seamless) {
        newTick = Math.min(newTick, animation.getActionsData().getFirst().getMovementData().size() - 1);
        int oldTick = globalTick;
        for (PlaybackData playbackData : entityPlaybacks) {
            ActionsData actionsData = playbackData.getActionsData();
            if(playbackData.getEntity() != null && playbackData.getEntity().equals(masterEntity)) {
                MovementData movementData = actionsData.getMovementData().get(newTick);
                playbackData.setLocalTick(newTick);
                if(seamless) {
                    moveEntitySilent(masterEntity, movementData);
                } else {
                    killMasterEntity();
                    spawnMasterEntity(movementData);
                    if(newTick == 0) {
                        globalTick = 0;
                        actionListener(playbackData);
                    }
                    for (int i = 0; i < newTick; i++) {
                        globalTick = i;
                        actionListener(playbackData);
                    }
                    globalTick = oldTick;
                }
            } else {
                playbackData.changeLocationByTick(newTick, seamless);
            }
        }
        for (PlaybackData playbackData : entityPlaybacks) {
            int tickDiff = newTick - globalTick;
            if(tickDiff > 0) {
                for (int i = globalTick; i < newTick; i++) {
                    globalTick = i;
                    actionListener(playbackData);
                }
            } else {
                for (int i = globalTick; i > newTick; i--) {
                    globalTick = i;
                    actionListenerRewind(playbackData);
                }
            }
            globalTick = oldTick;
        }
        this.globalTick = newTick;
        this.hasEnded = entityPlaybacks.stream().allMatch(PlaybackData::hasEnded);
    }

    public void actionListener(PlaybackData playbackData) {
        if(playbackData.getEntity() == null) return;
        List<Action> actionToBePlayed = playbackData.getActionsData().getActions().stream().filter(action -> globalTick == action.getTick()).toList();
        for(Action action : actionToBePlayed) {
            action.execute(playbackData);
        }
    }

    public void actionListenerRewind(PlaybackData playbackData) {
        List<Action> actionToBePlayed = playbackData.getActionsData().getActions().stream().filter(action -> globalTick == action.getTick()).toList();
        actionToBePlayed = actionToBePlayed.reversed();
        for(Action action : actionToBePlayed) {
            if(!(action instanceof DeathAction) && playbackData.getEntity() == null) continue;
            if (action instanceof PoseAction poseAction) {
                poseAction.rewind(playbackData);
                if (poseAction.getPreviousPose() == Pose.SLEEPING) {
                    SleepAction previousSleepAction = (SleepAction) playbackData.getActionsData()
                            .getActions()
                            .stream()
                            .filter(action1 -> globalTick <= action.getTick() && action1 instanceof SleepAction)
                            .toList()
                            .getLast();
                    if (previousSleepAction != null) {
                        previousSleepAction.execute(playbackData);
                    }
                }
            } else {
                action.rewind(playbackData);
            }
        }
    }

    private void spawnMasterEntity(MovementData loc) {
        if (masterEntity != null && masterEntity.isAlive()) {
            moveEntitySilent(masterEntity, loc);
            return;
        }

        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), character.getName());
        loadSkin();

        if (BuiltInRegistries.ENTITY_TYPE.getId(character.getEntityType()) == BuiltInRegistries.ENTITY_TYPE.getId(EntityType.PLAYER)) {
            masterEntity = new FakePlayer(serverLevel, gameProfile);
            masterEntity.getEntityData().set(PlayerFields.getDATA_PLAYER_MODE_CUSTOMISATION(), (byte) 0b01111111);
        } else {
            masterEntity = (LivingEntity) character.getEntityType().create(serverLevel, EntitySpawnReason.MOB_SUMMONED);
            if (masterEntity instanceof Mob mob) mob.setNoAi(true);
        }

        moveEntitySilent(masterEntity, loc);

        if (masterEntity instanceof FakePlayer fakePlayer) {
            ((PlayerListFields) serverLevel.getServer().getPlayerList()).getPlayersByUUID().put(fakePlayer.getUUID(), fakePlayer);
            serverLevel.getServer().getPlayerList().broadcastAll(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(fakePlayer)));
            serverLevel.addNewPlayer(fakePlayer);
        } else {
            serverLevel.addFreshEntity(masterEntity);
        }

        character.setEntity(masterEntity);
        entityPlaybacks.getFirst().setEntity(masterEntity);
        if(playbackType == PlaybackType.PRODUCTION) {
            StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
            if(storyHandler != null && storyHandler.isRunning()) {
                storyHandler.addCharacter(character);
            }
        }
    }

    public int getMaxTick() {
        return animation.getActionsData().stream()
                .mapToInt(data -> data.getMovementData().size())
                .max()
                .orElse(0);
    }

    private void loadSkin() {
        if (character.getCharacterType() == CharacterStory.CharacterType.MAIN) {
            File skinFile = playbackType == PlaybackType.DEVELOPMENT ?
                    NarrativeCraftFile.getSkinFile(character, animation.getSkinName()) :
                    character.getCharacterSkinController().getSkinFile(animation.getSkinName());
            character.getCharacterSkinController().setCurrentSkin(skinFile);
        }
    }

    public void stop() {
        isPlaying = false;
        hasEnded = true;
    }

    public void forceStop() {
        isPlaying = false;
        hasEnded = true;
        for(PlaybackData playbackData : entityPlaybacks) {
            playbackData.actionsData.reset(playbackData.entity);
            playbackData.killEntity();
        }
        if(playbackType == PlaybackType.PRODUCTION) {
            StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
            if(storyHandler != null && storyHandler.isRunning()) {
                storyHandler.removeCharacter(character);
            }
        }
    }

    private void moveEntitySilent(Entity entity, MovementData movementData) {
        if (entity == null) return;
        entity.setXRot(movementData.getXRot());
        entity.setYRot(movementData.getYRot());
        entity.setYHeadRot(movementData.getYHeadRot());
        entity.setOnGround(movementData.isOnGround());
        entity.teleportTo(movementData.getX(), movementData.getY(), movementData.getZ());
    }

    public boolean entityInPlayback(Entity entity) {
        for(PlaybackData playbackData : entityPlaybacks) {
            if(playbackData.entity != null) {
                if(playbackData.entity.getUUID().equals(entity.getUUID())) return true;
            }
        }
        return false;
    }

    public Entity getEntityByRecordId(int recordingId) {
        for(PlaybackData playbackData : entityPlaybacks) {
            if(playbackData.actionsData.getEntityIdRecording() == recordingId) {
                return playbackData.entity;
            }
        }
        return null;
    }

    public PlaybackData getPlaybackDataByRecordId(int recordingId) {
        for(PlaybackData playbackData : entityPlaybacks) {
            if(playbackData.actionsData.getEntityIdRecording() == recordingId) {
                return playbackData;
            }
        }
        return null;
    }

    public List<PlaybackData> getEntityPlaybacks() {
        return entityPlaybacks;
    }

    public ActionsData getMasterEntityData() {
        return animation.getActionsData().getFirst();
    }

    public boolean isPlaying() { return isPlaying; }
    public boolean hasEnded() { return hasEnded; }
    public int getTick() { return globalTick; }
    public void setTick(int tick) { this.globalTick = tick; }
    public void setPlaying(boolean playing) {
        this.isPlaying = playing;
        if (playing) this.hasEnded = false;
    }
    public Animation getAnimation() { return animation; }
    public LivingEntity getMasterEntity() { return masterEntity; }
    public CharacterStory getCharacter() { return character; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public PlaybackType getPlaybackType() { return playbackType; }
    public ServerLevel getServerLevel() { return serverLevel; }
    public boolean isLooping() { return isLooping; }
    public boolean isUnique() { return isUnique; }
    public void setUnique(boolean unique) { isUnique = unique; }

    public enum PlaybackType {
        DEVELOPMENT,
        PRODUCTION,
    }

    public static class PlaybackData {

        private final ActionsData actionsData;
        private final Playback playback;
        private Entity entity;
        private int localTick;

        public PlaybackData(ActionsData actionsData, Playback playback) {
            this.actionsData = actionsData;
            this.localTick = 0;
            this.playback = playback;
        }

        public void tick(int globalTick) {
            if (globalTick >= actionsData.getSpawnTick()) {
                if(entity == null) {
                    spawnEntity(actionsData.getMovementData().getFirst());
                }
            }

            if (entity == null) return;

            List<MovementData> movements = actionsData.getMovementData();
            if (localTick >= movements.size()) return;

            MovementData current = movements.get(localTick);
            MovementData next = localTick + 1 < movements.size() ? movements.get(localTick + 1) : current;

            moveEntity(current, next, false);

            localTick++;
        }

        public void changeLocationByTick(int newTick, boolean seamless) {
            if (newTick >= actionsData.getSpawnTick()) {
                if(entity == null) {
                    spawnEntity(actionsData.getMovementData().getFirst());
                }
            } else {
                killEntity();
                reset();
                return;
            }
            localTick = newTick - actionsData.getSpawnTick();
            MovementData movementData = actionsData.getMovementData().get(localTick);
            if(seamless) {
                moveEntity(movementData, movementData, true);
            } else {
                killEntity();
                spawnEntity(movementData);
            }
        }

        public void killEntity() {
            if(entity == null) return;
            entity.remove(Entity.RemovalReason.KILLED);
            if(entity instanceof FakePlayer fakePlayer) {
                NarrativeCraftMod.server.getPlayerList().remove(fakePlayer);
                ((PlayerListFields)NarrativeCraftMod.server.getPlayerList()).getPlayersByUUID().remove(fakePlayer.getUUID());
            }
            entity = null;
        }

        public void spawnEntity(MovementData location) {
            ServerLevel serverLevel = Utils.getServerLevel();
            if(actionsData.getEntityId() == BuiltInRegistries.ENTITY_TYPE.getId(EntityType.PLAYER)) return;
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.byId(actionsData.getEntityId());
            entity = entityType.create(serverLevel, EntitySpawnReason.MOB_SUMMONED);
            if(entity == null) return;
            try {
                entity.load(Utils.nbtFromString(actionsData.getNbtData()));
            } catch (CommandSyntaxException e) {
                NarrativeCraftMod.LOG.error("Unexpected error when trying to load nbt entity data! ", e);
            }
            if (entity instanceof Mob mob) mob.setNoAi(true);
            moveEntity(location, location, true);
            if(entity instanceof ItemEntity itemEntity) { // Drop Item
                List<Action> actions = playback.getMasterEntityData().getActions().stream().filter(action -> action instanceof BreakBlockAction && action.getTick() == playback.getTick() - 1).toList();
                boolean randomizeMotion = !actions.isEmpty();
                entity = ((LivingEntityFields)playback.getMasterEntity()).callCreateItemStackToDrop(itemEntity.getItem(), randomizeMotion, false);
            }
            serverLevel.addFreshEntity(entity);
        }

        private void moveEntity(MovementData current, MovementData next, boolean silent) {
            if(entity == null) return;
            entity.setXRot(current.getXRot());
            entity.setYRot(current.getYRot());
            entity.setYHeadRot(current.getYHeadRot());
            entity.setOnGround(current.isOnGround());
            entity.teleportTo(current.getX(), current.getY(), current.getZ());
            if(!silent) {
                entity.move(MoverType.SELF, MovementUtils.getDeltaMovement(current, next));
            }
        }

        public void reset() {
            this.localTick = 0;
            actionsData.reset(entity);
        }

        public boolean hasEnded() {
            return localTick >= actionsData.getMovementData().size();
        }

        public ActionsData getActionsData() {
            return actionsData;
        }

        public int getLocalTick() {
            return localTick;
        }

        public void setLocalTick(int localTick) {
            this.localTick = localTick;
        }

        public Entity getEntity() {
            return entity;
        }

        public void setEntity(Entity entity) {
            this.entity = entity;
        }

        public Playback getPlayback() {
            return playback;
        }
    }

}
