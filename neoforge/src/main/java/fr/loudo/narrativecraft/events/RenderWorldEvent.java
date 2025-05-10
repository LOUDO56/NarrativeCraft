package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(NarrativeCraftMod.MOD_ID)
public class RenderWorldEvent {

    public RenderWorldEvent(IEventBus eventBus) {
        NeoForge.EVENT_BUS.addListener(RenderWorldEvent::onWorldRender);
    }

    private static void onWorldRender(RenderLevelStageEvent event) {
        //TODO: line render is bugged?
        OnRenderWorld.renderWorld(event.getPoseStack());
    }

}
