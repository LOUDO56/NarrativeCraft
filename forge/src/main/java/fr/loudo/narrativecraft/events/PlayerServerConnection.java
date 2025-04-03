package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.Constants;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerServerConnection {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        OnPlayerServerConnection.playerJoin((ServerPlayer) event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        OnPlayerServerConnection.playerLeave((ServerPlayer) event.getEntity());

    }
}
