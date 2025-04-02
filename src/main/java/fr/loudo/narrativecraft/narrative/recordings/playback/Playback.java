package fr.loudo.narrativecraft.narrative.recordings.playback;

import com.mojang.authlib.GameProfile;
import fr.loudo.narrativecraft.NarrativeCraft;
import fr.loudo.narrativecraft.narrative.animations.Animation;
import fr.loudo.narrativecraft.narrative.recordings.MovementData;
import fr.loudo.narrativecraft.utils.FakePlayer;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;

import java.util.UUID;

public class Playback {

    private Animation animation;
    private FakePlayer fakePlayer;
    private ServerLevel serverLevel;
    private boolean isPlaying;

    private int index;

    public Playback(Animation animation, ServerLevel serverLevel) {
        this.animation = animation;
        this.serverLevel = serverLevel;
        this.isPlaying = false;
    }

    public boolean start() {
        if(isPlaying) return false;
        index = 0;
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "fakeP");
        fakePlayer = new FakePlayer(serverLevel, gameProfile);
        MovementData firstLoc = animation.getLocations().getFirst();
        fakePlayer.moveTo(firstLoc.getX(), firstLoc.getY(), firstLoc.getZ());
        serverLevel.getServer().getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, fakePlayer));
        serverLevel.addFreshEntity(fakePlayer);
        isPlaying = true;
        NarrativeCraft.getInstance().getPlaybackHandler().getPlaybacks().add(this);
        return true;
    }

    public boolean stop() {
        if(!isPlaying) return false;
        fakePlayer.remove(Entity.RemovalReason.KILLED);
        isPlaying = false;
        //NarrativeCraft.getInstance().getPlaybackHandler().getPlaybacks().remove(this);
        return true;
    }

    public void next() {
        if(index >= animation.getLocations().size() - 1) {
            stop();
            return;
        };

        MovementData movementData = animation.getLocations().get(index);
        MovementData movementDataNext = animation.getLocations().get(index);
        if (index < animation.getLocations().size() - 1) {
            movementDataNext = animation.getLocations().get(index + 1);
        }
        fakePlayer.setXRot(movementData.getXRot());
        fakePlayer.setYRot(movementData.getYRot());
        fakePlayer.setYHeadRot(movementData.getYHeadRot());
        fakePlayer.setOnGround(movementData.isOnGround());
        fakePlayer.move(MoverType.PLAYER, MovementData.getDeltaMovement(movementData, movementDataNext));
//
//        PositionMoveRotation positionMoveRotation = new PositionMoveRotation(pos, new Vec3(0, 0, 0), fakePlayer.getYRot(), fakePlayer.getXRot());
//        for(ServerPlayer serverPlayer : serverLevel.getServer().getPlayerList().getPlayers()) {
//            serverPlayer.connection.send(new ClientboundEntityPositionSyncPacket(fakePlayer.getId(), positionMoveRotation, true));
//        }

        index++;
    }

    public boolean isPlaying() {
        return isPlaying;
    }
}
