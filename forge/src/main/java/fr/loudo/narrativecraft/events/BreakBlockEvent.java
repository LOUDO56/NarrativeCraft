package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NarrativeCraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BreakBlockEvent {

    @SubscribeEvent
    public static void onBreakBlock(BlockEvent.BreakEvent event) {
        ServerPlayer serverPlayer = event.getPlayer().getServer().getPlayerList().getPlayer(event.getPlayer().getUUID());
        OnBreakBlock.breakBlock(event.getPos(), serverPlayer);
    }
}
