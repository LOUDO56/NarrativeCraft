package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    @Inject(method = "shouldShowName(Lnet/minecraft/world/entity/Entity;D)Z", at = @At(value = "HEAD"), cancellable = true)
    private void shouldShowName(Entity par1, double par2, CallbackInfoReturnable<Boolean> cir) {
        if(NarrativeCraftMod.getInstance().isCutsceneMode()) {
            cir.setReturnValue(false);
        }
    }


}
