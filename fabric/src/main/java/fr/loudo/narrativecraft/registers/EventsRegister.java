package fr.loudo.narrativecraft.registers;

import fr.loudo.narrativecraft.events.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class EventsRegister {

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(LifecycleEvent::onServerStart);
        ServerPlayConnectionEvents.JOIN.register(PlayerServerConnection::onPlayerJoin);
        ServerPlayConnectionEvents.DISCONNECT.register(PlayerServerConnection::onPlayerLeave);
        ServerPlayerEvents.AFTER_RESPAWN.register(RespawnEvent::onRespawn);
        ServerTickEvents.END_SERVER_TICK.register(ServerTickEvent::onServerTick);
        PlayerBlockBreakEvents.BEFORE.register(BlockBreakEvent::onBlockBreak);
        UseBlockCallback.EVENT.register(RightClickBlock::onRightClickBlock);
        UseEntityCallback.EVENT.register(EntityRightClick::onEntityRightClick);
        ClientTickEvents.END_CLIENT_TICK.register(OnClientTick::clientTick);
        HudLayerRegistrationCallback.EVENT.register(HudRender::keyframeControllerBaseHUDRender);
        HudLayerRegistrationCallback.EVENT.register(HudRender::dialogHud);
        HudLayerRegistrationCallback.EVENT.register(HudRender::borderHud);
        HudLayerRegistrationCallback.EVENT.register(HudRender::fadeHUDRender);
        HudLayerRegistrationCallback.EVENT.register(HudRender::saveIconRender);
        HudLayerRegistrationCallback.EVENT.register(HudRender::loadingRender);
        WorldRenderEvents.LAST.register(RenderWorldEvent::renderWorld);
    }

}
