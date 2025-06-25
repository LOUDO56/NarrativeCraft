package fr.loudo.narrativecraft.events;


import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@Mod(NarrativeCraftMod.MOD_ID)
public class EntityRightClick {

    public EntityRightClick(IEventBus bus) {
        NeoForge.EVENT_BUS.addListener(EntityRightClick::onEntityRightClick);
    }

    public static void onEntityRightClick(PlayerInteractEvent.EntityInteract event) {
        if(event.getLevel().isClientSide) {
            ServerPlayer serverPlayer = NarrativeCraftMod.server.getPlayerList().getPlayer(event.getEntity().getUUID());
            OnEntityRightClick.entityRightClick(serverPlayer, event.getTarget());
        }
    }
}
