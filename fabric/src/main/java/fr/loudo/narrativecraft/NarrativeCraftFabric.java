package fr.loudo.narrativecraft;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import fr.loudo.narrativecraft.events.HudRender;
import fr.loudo.narrativecraft.registers.CommandsRegister;
import fr.loudo.narrativecraft.registers.EventsRegister;
import fr.loudo.narrativecraft.registers.ModKeysRegister;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class NarrativeCraftFabric implements ModInitializer {

    @Override
    public void onInitialize() {

        CommandsRegister.register();
        EventsRegister.register();
        ModKeysRegister.register();

        RenderPipeline pipeline = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.TEXT_SNIPPET)
                .withLocation("pipeline/text_background_see_through")
                .withVertexShader("core/rendertype_text_background_see_through")
                .withFragmentShader("core/rendertype_text_background_see_through")
                .withDepthWrite(false)
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
                .build()
        );

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
