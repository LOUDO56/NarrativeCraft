package fr.loudo.narrativecraft.narrative.playback;

import com.mojang.authlib.GameProfile;
import fr.loudo.narrativecraft.NarrativeCraft;
import fr.loudo.narrativecraft.narrative.animations.Animation;
import fr.loudo.narrativecraft.utils.FakePlayer;
import fr.loudo.narrativecraft.utils.Location;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

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
        for(ServerPlayer serverPlayer : serverLevel.getServer().getPlayerList().getPlayers()) {
            serverPlayer.connection.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, fakePlayer));
        }
        serverLevel.addNewPlayer(fakePlayer);
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

        Location location = animation.getLocations().get(index);
        fakePlayer.setXRot(location.getXRot());
        fakePlayer.setYRot(location.getYRot());
        fakePlayer.setYHeadRot(location.getYHeadRot());
        fakePlayer.moveTo(location.getX(), location.getY(), location.getZ(), location.getYRot(), location.getXRot());
        index++;
    }

    public boolean isPlaying() {
        return isPlaying;
    }
}
