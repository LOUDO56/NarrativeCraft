package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class HurtAction extends Action {
    public HurtAction(int waitTick) {
        super(waitTick, ActionType.HURT);
    }

    @Override
    public void execute(Entity entity) {
        entity.getServer().getPlayerList().broadcastAll(new ClientboundHurtAnimationPacket(entity.getId(), 0F));
        DamageSource damageSource = new DamageSource(entity.damageSources().generic().typeHolder());
        entity.handleDamageEvent(damageSource);
    }

    @Override
    public void rewind(Entity entity) {}
}
