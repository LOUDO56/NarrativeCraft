package fr.loudo.narrativecraft.narrative.recordings.playback;

import com.mojang.authlib.GameProfile;
import fr.loudo.narrativecraft.NarrativeCraft;
import fr.loudo.narrativecraft.narrative.animations.Animation;
import fr.loudo.narrativecraft.narrative.recordings.MovementData;
import fr.loudo.narrativecraft.narrative.recordings.actions.Action;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionExecution;
import fr.loudo.narrativecraft.utils.FakePlayer;
import fr.loudo.narrativecraft.utils.MovementUtils;
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

    private int indexMovement;
    private int indexAction;
    private int waitTickAction;
    private Action currentAction;

    public Playback(Animation animation, ServerLevel serverLevel) {
        this.animation = animation;
        this.serverLevel = serverLevel;
        this.isPlaying = false;
    }

    public boolean start() {
        if(isPlaying) return false;
        indexMovement = 0;
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "fakeP");
        fakePlayer = new FakePlayer(serverLevel, gameProfile);
        MovementData firstLoc = animation.getActionsData().getMovementData().getFirst();
        fakePlayer.moveTo(firstLoc.getX(), firstLoc.getY(), firstLoc.getZ());
        serverLevel.getServer().getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, fakePlayer));
        serverLevel.addFreshEntity(fakePlayer);
        currentAction = getFirst();
        indexAction = 0;
        waitTickAction = animation.getActionsData().getActions().getFirst().getWaitTick();
        isPlaying = true;
        NarrativeCraft.getInstance().getPlaybackHandler().getPlaybacks().add(this);
        return true;
    }

    private Action getFirst() {
        return animation.getActionsData().getActions().getFirst();
    }

    public boolean stop() {
        if(!isPlaying) return false;
        fakePlayer.remove(Entity.RemovalReason.KILLED);
        isPlaying = false;
        //NarrativeCraft.getInstance().getPlaybackHandler().getPlaybacks().remove(this);
        return true;
    }

    public void next() {
        List<MovementData> movementDataList = animation.getActionsData().getMovementData();
        if(indexMovement >= movementDataList.size() - 1) {
            stop();
            return;
        };

        MovementData movementData = movementDataList.get(indexMovement);
        MovementData movementDataNext = movementDataList.get(indexMovement);
        if (indexMovement < movementDataList.size() - 1) {
            movementDataNext = movementDataList.get(indexMovement + 1);
        }
        fakePlayer.setXRot(movementData.getXRot());
        fakePlayer.setYRot(movementData.getYRot());
        fakePlayer.setYHeadRot(movementData.getYHeadRot());
        fakePlayer.setOnGround(movementData.isOnGround());
        fakePlayer.move(MoverType.PLAYER, MovementUtils.getDeltaMovement(movementData, movementDataNext));
        actionListener();
//
//        PositionMoveRotation positionMoveRotation = new PositionMoveRotation(pos, new Vec3(0, 0, 0), fakePlayer.getYRot(), fakePlayer.getXRot());
//        for(ServerPlayer serverPlayer : serverLevel.getServer().getPlayerList().getPlayers()) {
//            serverPlayer.connection.send(new ClientboundEntityPositionSyncPacket(fakePlayer.getId(), positionMoveRotation, true));
//        }

        indexMovement++;
    }

    public void actionListener() {
        if(waitTickAction == 0) {
            ActionExecution.execute(fakePlayer, currentAction);
            if(indexAction < animation.getActionsData().getActions().size() - 1) {
                Action nextAction = animation.getActionsData().getActions().get(indexAction + 1);
                currentAction = nextAction;
                waitTickAction = nextAction.getWaitTick();
                indexAction++;
            } else {
                waitTickAction = -1;
            }
        }
        waitTickAction--;
    }


    public boolean isPlaying() {
        return isPlaying;
    }
}
