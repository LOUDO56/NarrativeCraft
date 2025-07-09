package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@Mod(NarrativeCraftMod.MOD_ID)
public class RespawnEvent {

    public RespawnEvent(IEventBus eventBus) {
        NeoForge.EVENT_BUS.addListener(RespawnEvent::respawnEvent);
    }

    private static void respawnEvent(PlayerEvent.PlayerRespawnEvent event) {
        OnRespawn.respawn(event.getEntity());
    }
}
