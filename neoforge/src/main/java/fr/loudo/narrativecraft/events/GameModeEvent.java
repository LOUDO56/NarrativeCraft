package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@Mod(NarrativeCraftMod.MOD_ID)
public class GameModeEvent {

    public GameModeEvent(IEventBus eventBus) {
        NeoForge.EVENT_BUS.addListener(GameModeEvent::attackEvent);
    }

    private static void attackEvent(PlayerEvent.PlayerChangeGameModeEvent event) {
       OnGameModeChange.gameModeChange(event.getNewGameMode(), event.getCurrentGameMode(), event.getEntity());
    }
}
