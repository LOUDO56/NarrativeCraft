package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NarrativeCraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BreakBlockEvent {

    @SubscribeEvent
    public static void onBreakBlock(BlockEvent.BreakEvent event) {
        OnBreakBlock.breakBlock(event.getState(), event.getPos(), Utils.getServerPlayerByUUID(event.getPlayer().getUUID()));
    }
}
