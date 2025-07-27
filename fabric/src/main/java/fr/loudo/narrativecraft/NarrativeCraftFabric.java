package fr.loudo.narrativecraft;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import fr.loudo.narrativecraft.registers.CommandsRegister;
import fr.loudo.narrativecraft.registers.EventsRegister;
import fr.loudo.narrativecraft.registers.ModKeysRegister;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class NarrativeCraftFabric implements ModInitializer {

    @Override
    public void onInitialize() {

        CommandsRegister.register();
        EventsRegister.register();
        ModKeysRegister.register();

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
