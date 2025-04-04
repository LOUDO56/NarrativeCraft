package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

@Mod(NarrativeCraftMod.MOD_ID)
public class PlaceBlockEvent {

    public PlaceBlockEvent(IEventBus eventBus) {
        NeoForge.EVENT_BUS.addListener(PlaceBlockEvent::onPlaceBlock);
    }

    private static void onPlaceBlock(BlockEvent.EntityPlaceEvent event) {
        if(event.getEntity() instanceof ServerPlayer serverPlayer) {
            OnPlaceBlock.placeBlock(event.getPlacedBlock(), event.getPos(), serverPlayer);
        }
    }
}
