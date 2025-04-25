package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod(NarrativeCraftMod.MOD_ID)
public class ServerTickEventNeoForge {

    public ServerTickEventNeoForge(IEventBus eventBus) {
        NeoForge.EVENT_BUS.addListener(ServerTickEventNeoForge::onServerTick);
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        OnServerTick.serverTick();
    }

}
