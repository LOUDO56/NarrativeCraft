package fr.loudo.narrativecraft.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.events.OnHudRender;
import fr.loudo.narrativecraft.events.OnRenderWorld;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(method = "renderEntities", at = @At("HEAD"))
    private void onRenderEntities(PoseStack p_369689_, MultiBufferSource.BufferSource p_367493_, Camera p_368044_, DeltaTracker p_369396_, List<Entity> p_364182_, CallbackInfo ci) {
        OnRenderWorld.renderWorld(p_369689_);
    }
}
