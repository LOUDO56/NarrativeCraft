package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.utils.Utils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@Mod(NarrativeCraftMod.MOD_ID)
public class RightClickBlockEvent {

    public RightClickBlockEvent(IEventBus eventBus) {
        NeoForge.EVENT_BUS.addListener(RightClickBlockEvent::onRightClickBlockEvent);
    }

    private static void onRightClickBlockEvent(PlayerInteractEvent.RightClickBlock event) {
        if(event.getSide() == LogicalSide.CLIENT) {
            OnRightClickBlock.onRightClick(event.getFace(), event.getPos(), event.getHand(), event.getHitVec().isInside(), Utils.getServerPlayerByUUID(event.getEntity().getUUID()));
        }
    }

}
