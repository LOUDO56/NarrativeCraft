package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.keys.ModKeys;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NarrativeCraftMod.MOD_ID, value = Dist.CLIENT)
public class KeyRegisterEvent {

    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        for(KeyMapping key : ModKeys.getAllKeys()) {
            event.register(key);
        }
    }

}
