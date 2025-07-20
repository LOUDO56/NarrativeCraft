package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.utils.FakePlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    // Prevents the creation of multiple uuid files (like, a lot)
    @Inject(method = "save", at = @At("HEAD"), cancellable = true)
    private void narrativecraft$playerStatSave(ServerPlayer player, CallbackInfo ci) {
        if(player instanceof FakePlayer) ci.cancel();
    }
}
