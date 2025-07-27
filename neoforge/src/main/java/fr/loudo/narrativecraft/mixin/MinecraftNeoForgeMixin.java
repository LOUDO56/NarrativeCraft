package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.events.OnLoadFinished;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftNeoForgeMixin {

    @Inject(method = "onResourceLoadFinished", at = @At("HEAD"))
    private void narrativecraft$gameLoadFinished(CallbackInfo ci)  {
        OnLoadFinished.loadFinished();
    }

}
