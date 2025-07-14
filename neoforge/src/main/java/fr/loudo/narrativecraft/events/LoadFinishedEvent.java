package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientResourceLoadFinishedEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod(NarrativeCraftMod.MOD_ID)
public class LoadFinishedEvent {

    public LoadFinishedEvent(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(LoadFinishedEvent::onLoadFinished);
    }

    private static void onLoadFinished(ClientResourceLoadFinishedEvent event) {
        OnLoadFinished.loadFinished();
    }
}
