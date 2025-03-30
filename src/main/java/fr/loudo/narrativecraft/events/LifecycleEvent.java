package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraft;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NarrativeCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LifecycleEvent {

    @SubscribeEvent
    public static void onServerStart(ServerStartedEvent startedEvent) {
        NarrativeCraftFile.init(startedEvent.getServer());
    }
}
