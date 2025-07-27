package fr.loudo.narrativecraft.events;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4fStack;

@Mod(NarrativeCraftMod.MOD_ID)
public class RenderWorldEvent {

    public RenderWorldEvent(IEventBus eventBus) {
        NeoForge.EVENT_BUS.addListener(RenderWorldEvent::attackEvent);
    }

    private static void attackEvent(RenderLevelStageEvent event) {
       if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
           Matrix4fStack matrix4fstack = RenderSystem.getModelViewStack();
           matrix4fstack.pushMatrix();
           matrix4fstack.mul(event.getModelViewMatrix());
           RenderSystem.applyModelViewMatrix();
           RenderSystem.depthMask(false);
           RenderSystem.disableDepthTest();
           OnRenderWorld.renderWorld(new PoseStack());
           matrix4fstack.popMatrix();
           RenderSystem.depthMask(true);
           RenderSystem.enableDepthTest();
       }
    }
}
