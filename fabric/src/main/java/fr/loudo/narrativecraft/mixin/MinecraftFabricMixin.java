package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.events.OnAttack;
import fr.loudo.narrativecraft.events.OnLoadFinished;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftFabricMixin {

    @Redirect(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/HitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;"))
    private HitResult.Type narrativecraft$onAttack(HitResult instance) {
        if(OnAttack.cancelAttack()) {
            return HitResult.Type.MISS;
        }
        return instance.getType();
    }

    @Inject(method = "onResourceLoadFinished", at = @At("HEAD"))
    private void narrativecraft$gameLoadFinished(CallbackInfo ci)  {
        OnLoadFinished.loadFinished();
    }
}
