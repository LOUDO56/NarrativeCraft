package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NarrativeCraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntityRightClick {

    @SubscribeEvent
    public static void onEntityRightClick(PlayerInteractEvent.EntityInteract event) {
        if(event.getLevel().isClientSide && NarrativeCraftMod.server != null) {
            ServerPlayer serverPlayer = NarrativeCraftMod.server.getPlayerList().getPlayer(event.getEntity().getUUID());
            OnEntityRightClick.entityRightClick(serverPlayer, event.getTarget());
        }
    }
}
