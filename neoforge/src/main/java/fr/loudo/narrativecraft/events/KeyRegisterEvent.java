package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.keys.ModKeys;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(NarrativeCraftMod.MOD_ID)
public class KeyRegisterEvent {

    public KeyRegisterEvent(IEventBus bus) {
        bus.addListener(KeyRegisterEvent::onKeyRegister);
    }

    private static void onKeyRegister(RegisterKeyMappingsEvent event) {
        for(KeyMapping key : ModKeys.getAllKeys()) {
            event.register(key);
        }
    }

}

