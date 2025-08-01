package fr.loudo.narrativecraft.events;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4fStack;

@Mod(NarrativeCraftMod.MOD_ID)
public class RenderWorldEvent {

    public RenderWorldEvent(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(RenderWorldEvent::onWorldRender);
    }

    private static void onWorldRender(RenderLevelStageEvent.AfterLevel event) {
        Matrix4fStack matrix4fstack = RenderSystem.getModelViewStack();
        matrix4fstack.pushMatrix();
        matrix4fstack.mul(event.getModelViewMatrix());
        OnRenderWorld.renderWorld(new PoseStack());
        matrix4fstack.popMatrix();
    }

}
