package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.events.OnAttack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Redirect(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/HitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;"))
    private HitResult.Type onAttack(HitResult instance) {
        if(OnAttack.cancelAttack()) {
            return HitResult.Type.MISS;
        }
        return instance.getType();
    }
}
