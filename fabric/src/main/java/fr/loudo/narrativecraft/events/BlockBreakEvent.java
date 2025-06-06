
package fr.loudo.narrativecraft.events;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockBreakEvent {
    public static void onBlockBreak(Level level, Player player, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity) {
        if(NarrativeCraftMod.server != null) {
            ServerPlayer serverPlayer = NarrativeCraftMod.server.getPlayerList().getPlayer(player.getUUID());
            OnBreakBlock.breakBlock(blockState, blockPos, serverPlayer);
        }
    }
}
