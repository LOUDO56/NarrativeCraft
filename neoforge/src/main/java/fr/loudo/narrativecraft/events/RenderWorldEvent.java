package fr.loudo.narrativecraft.events;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;

@Mod(NarrativeCraftMod.MOD_ID)
public class RenderWorldEvent {

    public RenderWorldEvent(IEventBus eventBus) {
        NeoForge.EVENT_BUS.addListener(RenderWorldEvent::onWorldRender);
    }

    private static void onWorldRender(RenderLevelStageEvent event) {
        if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            Quaternionf quaternionf = camera.rotation().conjugate(new Quaternionf());
            Matrix4f frustumMatrix = new Matrix4f().rotation(quaternionf);
            Matrix4fStack matrix4fstack = RenderSystem.getModelViewStack();
            matrix4fstack.pushMatrix();
            matrix4fstack.mul(frustumMatrix);
            OnRenderWorld.renderWorld(event.getPoseStack());
            matrix4fstack.popMatrix();
        }
    }

}
