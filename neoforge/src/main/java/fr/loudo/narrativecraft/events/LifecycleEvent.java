package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod(NarrativeCraftMod.MOD_ID)
public class LifecycleEvent {

    public LifecycleEvent(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(LifecycleEvent::onServerStart);
    }

    private static void onServerStart(ServerStartedEvent startedEvent) {
        OnLifecycle.execute(startedEvent.getServer());
    }
}
