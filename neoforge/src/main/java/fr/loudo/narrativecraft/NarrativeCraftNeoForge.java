package fr.loudo.narrativecraft;


import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(NarrativeCraftMod.MOD_ID)
public class NarrativeCraftNeoForge {

    public NarrativeCraftNeoForge(IEventBus eventBus) {
        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        // Use NeoForge to bootstrap the Common mod.
        NarrativeCraftMod.LOG.info("Hello NeoForge world!");
        CommonClass.init();
        NarrativeCraftMod.dialogBackgroundRenderType = RenderType.create(
                "narrativecraft_dialog_background",
                DefaultVertexFormat.POSITION_COLOR,
                VertexFormat.Mode.QUADS,
                1536,
                false,
                true,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                        .setTextureState(RenderStateShard.NO_TEXTURE)
                        .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                        .setCullState(RenderStateShard.NO_CULL)
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .createCompositeState(false)
        );

    }
}
