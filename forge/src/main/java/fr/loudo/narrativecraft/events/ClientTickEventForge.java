package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NarrativeCraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientTickEventForge {

    @SubscribeEvent
    public static void onServerTick(TickEvent.ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.END) {
            OnClientTick.clientTick(Minecraft.getInstance());
        }
    }

}
