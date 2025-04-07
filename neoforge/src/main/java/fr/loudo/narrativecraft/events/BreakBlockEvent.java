package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

@Mod(NarrativeCraftMod.MOD_ID)
public class BreakBlockEvent {

    public BreakBlockEvent(IEventBus eventBus) {
        NeoForge.EVENT_BUS.addListener(BreakBlockEvent::onBreakBlock);
    }

    private static void onBreakBlock(BlockEvent.BreakEvent event) {
        ServerPlayer serverPlayer = Utils.getServerPlayerByUUID(event.getPlayer().getUUID());
        OnBreakBlock.breakBlock(event.getPos(), serverPlayer);
        ChestBlockEntity
    }
}
