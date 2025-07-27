package fr.loudo.narrativecraft.events;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

@EventBusSubscriber(modid = NarrativeCraftMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PipelineRegisterEvent {

    @SubscribeEvent
    private static void onPipelineRegister(RegisterRenderPipelinesEvent event) {
        RenderPipeline pipeline = RenderPipeline.builder(RenderPipelines.TEXT_SNIPPET)
                .withLocation("pipeline/narrativecraft_dialog_background")
                .withVertexShader("core/rendertype_text_background_see_through")
                .withFragmentShader("core/rendertype_text_background_see_through")
                .withDepthWrite(false)
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
                .build();
        event.registerPipeline(pipeline);
        NarrativeCraftMod.dialogBackgroundRenderType = RenderType.create(
                "narrativecraft_dialog_background",
                1536,
                false,
                true,
                pipeline,
                RenderType.CompositeState.builder()
                        .setTextureState(RenderStateShard.NO_TEXTURE).setLightmapState(RenderStateShard.LIGHTMAP).createCompositeState(false)
        );
    }


}
