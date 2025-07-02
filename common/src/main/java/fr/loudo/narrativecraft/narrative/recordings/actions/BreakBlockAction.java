package fr.loudo.narrativecraft.narrative.recordings.actions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class BreakBlockAction extends Action {

    private int x, y, z;
    private boolean drop;
    private String data;

    public BreakBlockAction(int tick, int x, int y, int z, boolean drop, String data) {
        super(tick, ActionType.BLOCK_BREAK);
        this.x = x;
        this.y = y;
        this.z = z;
        this.drop = drop;
        this.data = data;
    }

    public BlockPos getBlockPos() {
        return new BlockPos(x, y , z);
    }

    public String getData() {
        return data;
    }

    @Override
    public void execute(LivingEntity entity) {
        ServerLevel serverLevel = Utils.getServerLevel();
        serverLevel.destroyBlock(new BlockPos(x, y, z), drop);
    }

    @Override
    public void rewind(LivingEntity entity) {
        ServerLevel serverLevel = Utils.getServerLevel();
        try {
            CompoundTag compoundTag = Utils.nbtFromString(data);
            RegistryAccess registryAccess = entity.registryAccess();
            BlockState blockState = NbtUtils.readBlockState(registryAccess.lookupOrThrow(Registries.BLOCK), compoundTag);
            BlockPos blockPos = new BlockPos(x, y, z);
            serverLevel.setBlock(blockPos, blockState, 3);
        } catch (CommandSyntaxException ignored) {}
    }
}
