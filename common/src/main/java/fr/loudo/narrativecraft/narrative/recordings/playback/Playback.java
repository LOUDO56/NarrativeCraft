package fr.loudo.narrativecraft.narrative.recordings.playback;

import com.mojang.authlib.GameProfile;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.recordings.MovementData;
import fr.loudo.narrativecraft.narrative.recordings.actions.*;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import fr.loudo.narrativecraft.utils.FakePlayer;
import fr.loudo.narrativecraft.utils.MovementUtils;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;

import java.util.List;
import java.util.UUID;

public class Playback {

    private Animation animation;
    private LivingEntity entity;
    private ServerLevel serverLevel;
    private boolean isPlaying, hasEnded;

    private int tick;

    public Playback(Animation animation, ServerLevel serverLevel) {
        this.animation = animation;
        this.serverLevel = serverLevel;
        this.isPlaying = false;
        this.hasEnded = false;
    }

    public boolean start() {
        if(isPlaying) return false;
        tick = 0;
        isPlaying = true;
        MovementData firstLoc = animation.getActionsData().getMovementData().getFirst();
        spawnEntity(firstLoc);
        NarrativeCraftMod.getInstance().getPlaybackHandler().getPlaybacks().add(this);
        return true;
    }

    private void spawnEntity(MovementData loc) {
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "fakeP");
        entity = new FakePlayer(serverLevel, gameProfile);
        moveEntitySilent(entity, loc);
        if(entity instanceof FakePlayer fakePlayer) {
            serverLevel.getServer().getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, fakePlayer));
            serverLevel.addNewPlayer(fakePlayer);
        } else {
            serverLevel.addFreshEntity(entity);
        }
    }

    public void stop() {
        isPlaying = false;
        hasEnded = true;
        //NarrativeCraft.getInstance().getPlaybackHandler().getPlaybacks().remove(this);
    }

    public void stopAndKill() {
        isPlaying = false;
        hasEnded = true;
        killEntity();
        //NarrativeCraft.getInstance().getPlaybackHandler().getPlaybacks().remove(this);
    }


    private void killEntity() {
        entity.remove(Entity.RemovalReason.KILLED);
        serverLevel.getServer().getPlayerList().broadcastAll(new ClientboundPlayerInfoRemovePacket(List.of(entity.getUUID())));
    }

    public void next() {
        if(!entity.isAlive()) return;
        List<MovementData> movementDataList = animation.getActionsData().getMovementData();
        if(tick >= movementDataList.size() - 1) {
            stop();
        };

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
        List<Action> actionToBePlayed = animation.getActionsData().getActions().stream().filter(action -> tick == action.getTick()).toList();
        for(Action action : actionToBePlayed) {
            if(action instanceof PlaceBlockAction placeBlockAction) {
                placeBlockAction.execute(entity, serverLevel);
            } else if(action instanceof BreakBlockAction breakBlockAction) {
                breakBlockAction.execute(serverLevel);
            } else if(action instanceof DestroyBlockStageAction destroyBlockStageAction) {
                destroyBlockStageAction.execute(serverLevel);
            } else if(action instanceof RightClickBlockAction rightClickBlockAction) {
                rightClickBlockAction.execute(entity);
            } else {
                action.execute(entity);
            }
        }
    }

    public void actionListenerRewind() {
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
            } else {
                action.execute(entity);
            }
        }
    }

    private void moveEntity(Entity entity, MovementData movementData, MovementData movementDataNext) {
        moveEntitySilent(entity, movementData);
        entity.move(MoverType.SELF, MovementUtils.getDeltaMovement(movementData, movementDataNext));
    }

    private void moveEntitySilent(Entity entity, MovementData movementData) {
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
        if(newTick < animation.getActionsData().getMovementData().size() - 1) {
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
            MovementData movementData = animation.getActionsData().getMovementData().get(newTick);
            if(seamless) {
                moveEntitySilent(entity, movementData);
            } else {
                killEntity();
                spawnEntity(movementData);
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
}
