package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.events.OnGameModeChange;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Shadow @Final public ServerPlayerGameMode gameMode;

    @Inject(method = "setGameMode", at = @At("HEAD"))
    private void narrativecraft$gameMode(GameType gameMode, CallbackInfoReturnable<Boolean> cir) {
        Player player = (ServerPlayer) (Object) this;
        OnGameModeChange.gameModeChange(gameMode, this.gameMode.getGameModeForPlayer(), player);
    }

}
