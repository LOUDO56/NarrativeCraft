package fr.loudo.narrativecraft.mixin.fields;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

@Mixin(PlayerList.class)
public interface PlayerListFields {

    @Accessor Map<UUID, ServerPlayer> getPlayersByUUID();

}
