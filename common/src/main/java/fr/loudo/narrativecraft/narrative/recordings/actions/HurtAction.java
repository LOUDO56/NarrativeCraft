package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.world.damagesource.DamageSource;

public class HurtAction extends Action {
    public HurtAction(int waitTick) {
        super(waitTick, ActionType.HURT);
    }

    @Override
    public void execute(Playback.PlaybackData playbackData) {
        playbackData.getEntity() .getServer().getPlayerList().broadcastAll(new ClientboundHurtAnimationPacket(playbackData.getEntity() .getId(), 0F));
        DamageSource damageSource = new DamageSource(playbackData.getEntity() .damageSources().generic().typeHolder());
        playbackData.getEntity().handleDamageEvent(damageSource);
    }

    @Override
    public void rewind(Playback.PlaybackData playbackData) {}
}
