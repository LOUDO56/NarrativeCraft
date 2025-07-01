package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(NarrativeCraftMod.MOD_ID)
public class AttackEvent {

    public AttackEvent(IEventBus eventBus) {
        NeoForge.EVENT_BUS.addListener(AttackEvent::attackEvent);
    }

    private static void attackEvent(InputEvent.InteractionKeyMappingTriggered event) {
        if(OnAttack.cancelAttack()) {
            event.setCanceled(true);
        }
    }
}
