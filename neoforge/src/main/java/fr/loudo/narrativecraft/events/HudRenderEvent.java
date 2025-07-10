package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(NarrativeCraftMod.MOD_ID)
public class HudRenderEvent {

    public HudRenderEvent(IEventBus eventBus) {
        NeoForge.EVENT_BUS.addListener(HudRenderEvent::onWorldRender);
    }

    private static void onWorldRender(RenderGuiEvent.Post event) {
        OnHudRender.keyframeControllerBaseRender(event.getGuiGraphics(), event.getPartialTick());
        OnHudRender.dialogHud(event.getGuiGraphics(), event.getPartialTick());
        OnHudRender.borderHud(event.getGuiGraphics(), event.getPartialTick());
        OnHudRender.fadeRender(event.getGuiGraphics(), event.getPartialTick());
        OnHudRender.saveIconRender(event.getGuiGraphics(), event.getPartialTick());
    }

}
