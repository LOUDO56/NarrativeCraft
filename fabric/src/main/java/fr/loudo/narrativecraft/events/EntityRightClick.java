package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

public class EntityRightClick {
    public static InteractionResult onEntityRightClick(Player player, Level level, InteractionHand hand, Entity entity, @Nullable EntityHitResult entityHitResult) {

        if(level.isClientSide) {
            ServerPlayer serverPlayer = NarrativeCraftMod.server.getPlayerList().getPlayer(player.getUUID());
            OnEntityRightClick.entityRightClick(serverPlayer, entity);
        }

        return InteractionResult.PASS;
    }
}
