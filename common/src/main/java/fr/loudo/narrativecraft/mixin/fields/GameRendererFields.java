package fr.loudo.narrativecraft.mixin.fields;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererFields {

    @Invoker void callBobView(PoseStack p_109139_, float p_109140_);
    @Invoker float callGetFov(Camera camera, float partialTick, boolean useFovSetting);
}
