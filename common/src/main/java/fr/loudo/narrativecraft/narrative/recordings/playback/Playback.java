package fr.loudo.narrativecraft.narrative.recordings.playback;

import com.mojang.authlib.GameProfile;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
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
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class Playback {

    private Animation animation;
    private CharacterStory character;
    private LivingEntity entity;
    private ServerLevel serverLevel;
    private PlaybackType playbackType;
    private boolean isPlaying, hasEnded;

    private int tick;

    public Playback(Animation animation, ServerLevel serverLevel, CharacterStory character, PlaybackType playbackType) {
        this.animation = animation;
        this.serverLevel = serverLevel;
        this.playbackType = playbackType;
        this.character = character;
        this.isPlaying = false;
        this.hasEnded = false;
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
        NarrativeCraftMod.getInstance().getPlaybackHandler().getPlaybacks().add(this);
        if(playbackType == PlaybackType.DEVELOPMENT) {
            NarrativeCraftMod.getInstance().getCharacterManager().reloadSkin(character);
        }
        actionListener();
        return true;
    }

    private void spawnEntity(MovementData loc) {
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), character.getName());
        loadSkin();
        entity = new FakePlayer(serverLevel, gameProfile);
        SynchedEntityData entityData = entity.getEntityData();
        EntityDataAccessor<Byte> ENTITY_LAYER = new EntityDataAccessor<>(17, EntityDataSerializers.BYTE);
        entityData.set(ENTITY_LAYER, (byte) 0b01111111);
        moveEntitySilent(entity, loc);
        if(entity instanceof FakePlayer fakePlayer) {
            serverLevel.getServer().getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, fakePlayer));
            serverLevel.addNewPlayer(fakePlayer);
        } else {
            serverLevel.addFreshEntity(entity);
        }
        character.setEntity(entity);
    }

    private void spawnEntity(MovementData loc, Entity oldEntity) {
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), character.getName());
        loadSkin();
        entity = new FakePlayer(serverLevel, gameProfile);
        character.setEntity(entity);
        moveEntitySilent(entity, loc);
        entity.setPose(oldEntity.getPose());
        SynchedEntityData entityData = entity.getEntityData();
        EntityDataAccessor<Byte> ENTITY_BYTE_MASK = new EntityDataAccessor<>(0, EntityDataSerializers.BYTE);
        EntityDataAccessor<Byte> ENTITY_LAYER = new EntityDataAccessor<>(17, EntityDataSerializers.BYTE);
        entityData.set(ENTITY_BYTE_MASK, oldEntity.getEntityData().get(ENTITY_BYTE_MASK));
        entityData.set(ENTITY_LAYER, (byte) 0b01111111);
        if(entity instanceof FakePlayer fakePlayer) {
            serverLevel.getServer().getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, fakePlayer));
            serverLevel.addNewPlayer(fakePlayer);
        } else {
            serverLevel.addFreshEntity(entity);
        }
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
    }

    public void stopAndKill() {
        isPlaying = false;
        hasEnded = true;
        killEntity();
    }


    private void killEntity() {
        if(entity == null) return;
        entity.remove(Entity.RemovalReason.KILLED);
        serverLevel.getServer().getPlayerList().broadcastAll(new ClientboundPlayerInfoRemovePacket(List.of(entity.getUUID())));
    }

    public void next() {
        if(entity == null) return;
        if(!entity.isAlive()) return;
        List<MovementData> movementDataList = animation.getActionsData().getMovementData();
        PlayerSession playerSession = Utils.getSessionOrNull(Minecraft.getInstance().player.getUUID());
        KeyframeControllerBase keyframeControllerBase = playerSession.getKeyframeControllerBase();
        if(tick >= movementDataList.size() - 1) {
            if(playbackType == PlaybackType.DEVELOPMENT && !(keyframeControllerBase instanceof CutsceneController)) {
                stopAndKill();
            } else {
                stop();
            }
        }


        MovementData movementData = movementDataList.get(tick);
        MovementData movementDataNext = movementDataList.get(tick);
        if (tick < movementDataList.size() - 1) {
            movementDataNext = movementDataList.get(tick + 1);
        }
        moveEntity(entity, movementData, movementDataNext);
        actionListener();
//
//        PositionMoveRotation positionMoveRotation = new PositionMoveRotation(pos, new Vec3(0, 0, 0), fakePlayer.getYRot(), fakePlayer.getXRot());
//        for(ServerPlayer serverPlayer : serverLevel.getServer().getPlayerList().getPlayers()) {
//            serverPlayer.connection.send(new ClientboundEntityPositionSyncPacket(fakePlayer.getId(), positionMoveRotation, true));
//        }

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
            if(action instanceof PlaceBlockAction placeBlockAction) {
                placeBlockAction.execute(entity, serverLevel);
            } else if(action instanceof BreakBlockAction breakBlockAction) {
                PlaceBlockAction placeBlockAction = new PlaceBlockAction(tick, ActionType.BLOCK_PLACE, breakBlockAction.getX(), breakBlockAction.getY(), breakBlockAction.getZ(), breakBlockAction.getData());
                placeBlockAction.execute(entity, serverLevel);
            } else if(action instanceof DestroyBlockStageAction destroyBlockStageAction) {
                destroyBlockStageAction.execute(serverLevel, destroyBlockStageAction.getProgress() == 1);
            } else if(action instanceof RightClickBlockAction rightClickBlockAction) {
                rightClickBlockAction.execute(entity);
            } else if(action instanceof PoseAction poseAction) {
                poseAction.execute(entity, true);
            } else if(action instanceof EntityByteAction entityByteAction) {
                entityByteAction.execute(entity, true);
            } else {
                action.execute(entity);
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
        if(newTick < animation.getActionsData().getMovementData().size() - 1) {
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
            hasEnded = false;
        } else {
            hasEnded = true;
        }
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

    public enum PlaybackType {
        DEVELOPMENT,
        PRODUCTION,
    }
}
