package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;

public class HurtAction extends Action {
    public HurtAction(int waitTick, ActionType actionType) {
        super(waitTick, actionType);
    }

    @Override
    public void execute(LivingEntity entity) {
        entity.getServer().getPlayerList().broadcastAll(new ClientboundHurtAnimationPacket(entity.getId(), 0F));
        entity.makeSound(SoundEvents.GENERIC_HURT);
    }
}
