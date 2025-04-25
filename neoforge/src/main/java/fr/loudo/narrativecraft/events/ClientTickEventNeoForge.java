package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(NarrativeCraftMod.MOD_ID)
public class ClientTickEventNeoForge {

    public ClientTickEventNeoForge(IEventBus eventBus) {
        NeoForge.EVENT_BUS.addListener(ClientTickEventNeoForge::onClientTick);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        OnClientTick.clientTick(Minecraft.getInstance());
    }

}
