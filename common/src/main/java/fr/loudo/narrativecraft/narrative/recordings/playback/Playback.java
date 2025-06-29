package fr.loudo.narrativecraft.narrative.recordings.playback;

import com.mojang.authlib.GameProfile;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.mixin.fields.PlayerListFields;
import fr.loudo.narrativecraft.narrative.chapter.scenes.KeyframeControllerBase;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.recordings.MovementData;
import fr.loudo.narrativecraft.narrative.recordings.actions.*;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.utils.FakePlayer;
import fr.loudo.narrativecraft.utils.MovementUtils;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class Playback {

    private int id;
    private boolean isLooping;
    private Animation animation;
    private CharacterStory character;
    private LivingEntity entity;
    private ServerLevel serverLevel;
    private PlaybackType playbackType;
    private boolean isPlaying, hasEnded;

    private int tick;

    public Playback(Animation animation, ServerLevel serverLevel, CharacterStory character, PlaybackType playbackType, boolean isLooping) {
        this.animation = animation;
        this.serverLevel = serverLevel;
        this.playbackType = playbackType;
        this.character = character;
        this.isPlaying = false;
        this.hasEnded = false;
        this.isLooping = isLooping;
    }

    public boolean start() {
        if(character == null) {
            Minecraft.getInstance().player.displayClientMessage(
                    Component.literal("§c" + Translation.message("playback.start.fail", animation.getName()).getString()),
                    false
            );
            return false;
        }
        if(isPlaying) return false;
        tick = 0;
        isPlaying = true;
        MovementData firstLoc = animation.getActionsData().getMovementData().getFirst();
        spawnEntity(firstLoc);
        NarrativeCraftMod.getInstance().getPlaybackHandler().addPlayback(this);
        if(playbackType == PlaybackType.DEVELOPMENT) {
            NarrativeCraftMod.getInstance().getCharacterManager().reloadSkin(character);
        }
        actionListener();
        return true;
    }

    private void spawnEntity(MovementData loc) {
        if(entity != null && entity.isAlive()) {
            moveEntitySilent(entity, loc);
            return;
        }
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), character.getName());
        loadSkin();
        if(BuiltInRegistries.ENTITY_TYPE.getId(character.getEntityType()) == BuiltInRegistries.ENTITY_TYPE.getId(EntityType.PLAYER)) {
            entity = new FakePlayer(serverLevel, gameProfile);
            SynchedEntityData entityData = entity.getEntityData();
            EntityDataAccessor<Byte> ENTITY_LAYER = new EntityDataAccessor<>(17, EntityDataSerializers.BYTE);
            entityData.set(ENTITY_LAYER, (byte) 0b01111111);
        } else {
            entity = (LivingEntity) character.getEntityType().create(serverLevel, EntitySpawnReason.MOB_SUMMONED);
            if(entity instanceof Mob mob) {
                mob.setNoAi(true);
                mob.setSilent(true);
            }
        }
        moveEntitySilent(entity, loc);
        if(entity instanceof FakePlayer fakePlayer) {
            serverLevel.getServer().getPlayerList().getPlayers().add(fakePlayer);
            ((PlayerListFields)serverLevel.getServer().getPlayerList()).getPlayersByUUID().put(fakePlayer.getUUID(), fakePlayer);
            serverLevel.getServer().getPlayerList().broadcastAll(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(fakePlayer)));
            serverLevel.addNewPlayer(fakePlayer);

        } else {
            serverLevel.addFreshEntity(entity);
        }
        character.setEntity(entity);
    }

    private void spawnEntity(MovementData loc, Entity oldEntity) {
        spawnEntity(loc);
        SynchedEntityData entityData = entity.getEntityData();
        EntityDataAccessor<Byte> ENTITY_BYTE_MASK = new EntityDataAccessor<>(0, EntityDataSerializers.BYTE);
        entityData.set(ENTITY_BYTE_MASK, oldEntity.getEntityData().get(ENTITY_BYTE_MASK));
        entity.setPose(oldEntity.getPose());
    }

    private void loadSkin() {
        if(character.getCharacterType() == CharacterStory.CharacterType.MAIN) {
            File skinFile = null;
            if(playbackType == PlaybackType.DEVELOPMENT) {
                skinFile = NarrativeCraftFile.getSkinFile(character, animation.getSkinName());
            } else if (playbackType == PlaybackType.PRODUCTION){
                skinFile = character.getCharacterSkinController().getSkinFile(animation.getSkinName());
            }
            character.getCharacterSkinController().setCurrentSkin(skinFile);
        }
    }

    public void stop() {
        isPlaying = false;
        hasEnded = true;
        if(isLooping) {
            start();
        }
    }

    public void stopAndKill() {
        MovementData firstLoc = animation.getActionsData().getMovementData().getFirst();
        MovementData lastLoc = animation.getActionsData().getMovementData().getLast();
        if(firstLoc.getVec3().distanceTo(lastLoc.getVec3()) >= 2) {
            killEntity();
        }
        stop();
    }

    public void forceStop() {
        isPlaying = false;
        hasEnded = true;
        killEntity();
    }


    private void killEntity() {
        if(entity == null) return;
        if(entity instanceof FakePlayer fakePlayer) {
            serverLevel.getServer().getPlayerList().remove(fakePlayer);
        } else {
            entity.remove(Entity.RemovalReason.KILLED);
        }
    }

    public void next() {
        if(entity == null) return;
        if(!entity.isAlive()) return;
        List<MovementData> movementDataList = animation.getActionsData().getMovementData();
        PlayerSession playerSession = Utils.getSessionOrNull(Minecraft.getInstance().player.getUUID());
        KeyframeControllerBase keyframeControllerBase = playerSession.getKeyframeControllerBase();
        if(tick >= movementDataList.size() - 1) {
            if(playbackType == PlaybackType.DEVELOPMENT) {
                if(!(keyframeControllerBase instanceof CutsceneController)) {
                    stopAndKill();
                } else {
                    stop();
                }
            } else {
                if(character.getCharacterType() == CharacterStory.CharacterType.NPC) {
                    stopAndKill();
                } else {
                    stop();
                }
            }
            return;
        }

        MovementData movementData = movementDataList.get(tick);
        MovementData movementDataNext = movementDataList.get(tick);
        if (tick < movementDataList.size() - 1) {
            movementDataNext = movementDataList.get(tick + 1);
        }
        moveEntity(entity, movementData, movementDataNext);
        actionListener();

        if(tick < movementDataList.size() - 1) {
            tick++;
        }
    }

    public void actionListener() {
        if(entity == null) return;
        List<Action> actionToBePlayed = animation.getActionsData().getActions().stream().filter(action -> tick == action.getTick()).toList();
        for(Action action : actionToBePlayed) {
            Action.parseAndExecute(action, entity);
        }
    }

    public void actionListenerRewind() {
        if(entity == null) return;
        List<Action> actionToBePlayed = animation.getActionsData().getActions().stream().filter(action -> tick == action.getTick()).toList();
        for(Action action : actionToBePlayed) {
            switch (action) {
                case PlaceBlockAction placeBlockAction -> placeBlockAction.execute(entity, serverLevel);
                case BreakBlockAction breakBlockAction -> {
                    PlaceBlockAction placeBlockAction = new PlaceBlockAction(tick, ActionType.BLOCK_PLACE, breakBlockAction.getX(), breakBlockAction.getY(), breakBlockAction.getZ(), breakBlockAction.getData());
                    placeBlockAction.execute(entity, serverLevel);
                }
                case DestroyBlockStageAction destroyBlockStageAction ->
                        destroyBlockStageAction.execute(serverLevel, destroyBlockStageAction.getProgress() == 1);
                case RightClickBlockAction rightClickBlockAction -> rightClickBlockAction.execute(entity);
                case PoseAction poseAction -> poseAction.execute(entity, true);
                case EntityByteAction entityByteAction -> entityByteAction.execute(entity, true);
                case null, default -> action.execute(entity);
            }
        }
    }

    private void moveEntity(Entity entity, MovementData movementData, MovementData movementDataNext) {
        if(entity == null) return;
        moveEntitySilent(entity, movementData);
        entity.move(MoverType.SELF, MovementUtils.getDeltaMovement(movementData, movementDataNext));
    }

    private void moveEntitySilent(Entity entity, MovementData movementData) {
        if(entity == null) return;
        entity.setXRot(movementData.getXRot());
        entity.setYRot(movementData.getYRot());
        entity.setYHeadRot(movementData.getYHeadRot());
        entity.setOnGround(movementData.isOnGround());
        entity.teleportTo(movementData.getX(), movementData.getY(), movementData.getZ());
    }


    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean hasEnded() {
        return hasEnded;
    }

    public void changeLocationByTick(int newTick, boolean seamless) {
        if(entity == null) return;
        newTick = Math.min(newTick, animation.getActionsData().getMovementData().size() - 1);
        MovementData movementData = animation.getActionsData().getMovementData().get(newTick);
        if(seamless) {
            moveEntitySilent(entity, movementData);
        } else {
            killEntity();
            spawnEntity(movementData, entity);
        }
        int tickDiff = newTick - tick;
        if(tickDiff > 0) {
            for (int i = tick; i < newTick; i++) {
                tick = i;
                actionListener();
            }
        } else {
            for (int i = tick; i > newTick; i--) {
                tick = i;
                actionListenerRewind();
            }
        }
        this.tick = newTick;
        List<Action> actions = animation.getActionsData().getActions().stream().filter(action -> tick >= action.getTick()).toList();
        for(Action action : actions) {
            Action.parseAndExecute(action, entity);
        }
        hasEnded = newTick == animation.getActionsData().getMovementData().size() - 1;
    }

    public void setTick(int tick) {
        this.tick = tick;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
        if(playing) {
            hasEnded = false;
        }
    }

    public Animation getAnimation() {
        return animation;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public CharacterStory getCharacter() {
        return character;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTick() {
        return tick;
    }

    public boolean isHasEnded() {
        return hasEnded;
    }

    public PlaybackType getPlaybackType() {
        return playbackType;
    }

    public ServerLevel getServerLevel() {
        return serverLevel;
    }

    public boolean isLooping() {
        return isLooping;
    }

    public enum PlaybackType {
        DEVELOPMENT,
        PRODUCTION,
    }
}
