package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@Mod(NarrativeCraftMod.MOD_ID)
public class DeathEvent {

    public DeathEvent(IEventBus eventBus) {
        NeoForge.EVENT_BUS.addListener(DeathEvent::deathEvent);
    }

    private static void deathEvent(LivingDeathEvent event) {
        OnDeath.death(event.getEntity());
    }
}
