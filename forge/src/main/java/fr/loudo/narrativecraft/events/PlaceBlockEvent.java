package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NarrativeCraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlaceBlockEvent {

    @SubscribeEvent
    public static void onPlaceBlock(BlockEvent.EntityPlaceEvent event) {
        if(event.getEntity() instanceof ServerPlayer serverPlayer) {
            OnPlaceBlock.placeBlock(event.getPlacedBlock(), event.getPos(), serverPlayer);
        }
    }
}
