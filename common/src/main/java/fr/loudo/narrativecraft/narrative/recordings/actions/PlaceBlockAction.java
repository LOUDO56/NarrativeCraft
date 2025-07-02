package fr.loudo.narrativecraft.narrative.recordings.actions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import fr.loudo.narrativecraft.utils.FakePlayer;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class PlaceBlockAction extends Action {

    private int x, y, z;
    private String data;

    public PlaceBlockAction(int tick, int x, int y, int z, String data) {
        super(tick, ActionType.BLOCK_PLACE);
        this.x = x;
        this.y = y;
        this.z = z;
        this.data = data;
    }

    @Override
    public void execute(LivingEntity entity) {
        ServerLevel serverLevel = Utils.getServerLevel();
        try {
            CompoundTag compoundTag = Utils.nbtFromString(data);
            RegistryAccess registryAccess = entity.registryAccess();
            BlockState blockState = NbtUtils.readBlockState(registryAccess.lookupOrThrow(Registries.BLOCK), compoundTag);
            BlockPos blockPos = new BlockPos(x, y, z);
            serverLevel.setBlock(blockPos, blockState, 3);
            SoundType soundType = blockState.getSoundType();
            serverLevel.playSound(entity, blockPos, blockState.getSoundType().getPlaceSound(),
                    SoundSource.BLOCKS, (soundType.getVolume() + 1.0f) / 2.0f, soundType.getPitch() * 0.8f);
        } catch (CommandSyntaxException ignored) {}
    }

    public void rewind(LivingEntity entity) {
        ServerLevel serverLevel = Utils.getServerLevel();
        serverLevel.setBlock(getBlockPos(), Blocks.AIR.defaultBlockState(), 3);
    }

    public BlockPos getBlockPos() {
        return new BlockPos(x, y, z);
    }
}
