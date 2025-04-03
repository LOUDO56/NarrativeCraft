package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.Constants;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod(Constants.MOD_ID)
public class ServerTickEventClass {

    public ServerTickEventClass(IEventBus eventBus) {
        NeoForge.EVENT_BUS.addListener(ServerTickEventClass::onServerTick);
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        OnServerTick.serverTick();
    }

}
