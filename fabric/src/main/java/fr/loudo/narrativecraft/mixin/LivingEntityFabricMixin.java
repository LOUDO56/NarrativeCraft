package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.events.OnDeath;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityFabricMixin {

    @Inject(method = "die", at = @At("HEAD"))
    private void narrativecraft$onLivingEntityDie(DamageSource damageSource, CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        OnDeath.death(livingEntity);
    }

}
