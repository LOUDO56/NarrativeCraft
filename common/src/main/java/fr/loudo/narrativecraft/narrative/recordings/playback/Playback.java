package fr.loudo.narrativecraft.narrative.recordings.playback;

import com.mojang.authlib.GameProfile;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.animations.Animation;
import fr.loudo.narrativecraft.narrative.recordings.MovementData;
import fr.loudo.narrativecraft.narrative.recordings.actions.*;
import fr.loudo.narrativecraft.utils.FakePlayer;
import fr.loudo.narrativecraft.utils.MovementUtils;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;

import java.util.List;
import java.util.UUID;

public class Playback {

    private Animation animation;
    private FakePlayer fakePlayer;
    private ServerLevel serverLevel;
    private boolean isPlaying;

    private int tick;

    public Playback(Animation animation, ServerLevel serverLevel) {
        this.animation = animation;
        this.serverLevel = serverLevel;
        this.isPlaying = false;
    }

    public boolean start() {
        if(isPlaying) return false;
        tick = 0;
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "fakeP");
        fakePlayer = new FakePlayer(serverLevel, gameProfile);
        MovementData firstLoc = animation.getActionsData().getMovementData().getFirst();
        moveEntitySilent(fakePlayer, firstLoc);
        serverLevel.getServer().getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, fakePlayer));
        serverLevel.addNewPlayer(fakePlayer);
        isPlaying = true;
        NarrativeCraftMod.getInstance().getPlaybackHandler().getPlaybacks().add(this);
        return true;
    }

    public boolean stop() {
        fakePlayer.remove(Entity.RemovalReason.KILLED);
        serverLevel.getServer().getPlayerList().broadcastAll(new ClientboundPlayerInfoRemovePacket(List.of(fakePlayer.getUUID())));
        isPlaying = false;
        //NarrativeCraft.getInstance().getPlaybackHandler().getPlaybacks().remove(this);
        return true;
    }

    public void next() {
        List<MovementData> movementDataList = animation.getActionsData().getMovementData();
        if(tick >= movementDataList.size() - 1) {
            stop();
            return;
        };

        MovementData movementData = movementDataList.get(tick);
        MovementData movementDataNext = movementDataList.get(tick);
        if (tick < movementDataList.size() - 1) {
            movementDataNext = movementDataList.get(tick + 1);
        }
        moveEntity(fakePlayer, movementData, movementDataNext);
        actionListener();
//
//        PositionMoveRotation positionMoveRotation = new PositionMoveRotation(pos, new Vec3(0, 0, 0), fakePlayer.getYRot(), fakePlayer.getXRot());
//        for(ServerPlayer serverPlayer : serverLevel.getServer().getPlayerList().getPlayers()) {
//            serverPlayer.connection.send(new ClientboundEntityPositionSyncPacket(fakePlayer.getId(), positionMoveRotation, true));
//        }

        tick++;
    }

    public void actionListener() {
        List<Action> actionToBePlayed = animation.getActionsData().getActions().stream().filter(action -> tick == action.getTick()).toList();
        for(Action action : actionToBePlayed) {
            if(action instanceof PlaceBlockAction placeBlockAction) {
                placeBlockAction.execute(fakePlayer, serverLevel);
            } else if(action instanceof BreakBlockAction breakBlockAction) {
                breakBlockAction.execute(serverLevel);
            } else if(action instanceof DestroyBlockStageAction destroyBlockStageAction) {
                destroyBlockStageAction.execute(serverLevel);
            } else if(action instanceof RightClickBlockAction rightClickBlockAction) {
                rightClickBlockAction.execute(fakePlayer);
            } else {
                action.execute(fakePlayer);
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

    public void changeLocationByTick(int tick) {
        if(tick < animation.getActionsData().getMovementData().size() - 1) {
            this.tick = tick;
            MovementData movementData = animation.getActionsData().getMovementData().get(tick);
            moveEntitySilent(fakePlayer, movementData);
        }
    }

    public void setTick(int tick) {
        this.tick = tick;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public Animation getAnimation() {
        return animation;
    }

    public FakePlayer getFakePlayer() {
        return fakePlayer;
    }
}
