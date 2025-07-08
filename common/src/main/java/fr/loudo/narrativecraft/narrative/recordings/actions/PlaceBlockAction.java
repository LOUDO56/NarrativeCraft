package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class PlaceBlockAction extends Action {

    private int x, y, z;
    private String data;

    public PlaceBlockAction(int tick, BlockPos blockPos, BlockState blockState) {
        super(tick, ActionType.BLOCK_PLACE);
        this.x = blockPos.getX();
        this.y = blockPos.getY();
        this.z = blockPos.getZ();
        this.data = NbtUtils.writeBlockState(blockState).toString();
    }

    @Override
    public void execute(Entity entity) {
        ServerLevel serverLevel = Utils.getServerLevel();
        BlockState blockState = Utils.getBlockStateFromData(data, entity.registryAccess());
        if(blockState == null) return;
        BlockPos blockPos = new BlockPos(x, y, z);
        serverLevel.setBlock(blockPos, blockState, 3);
        SoundType soundType = blockState.getSoundType();
        serverLevel.playSound(entity, blockPos, blockState.getSoundType().getPlaceSound(),
                SoundSource.BLOCKS, (soundType.getVolume() + 1.0f) / 2.0f, soundType.getPitch() * 0.8f);

    }

    public void rewind(Entity entity) {
        ServerLevel serverLevel = Utils.getServerLevel();
        BlockPos blockPos = getBlockPos();
        BlockState blockState = Utils.getBlockStateFromData(data, entity.registryAccess());
        if (blockState != null) {
            if (blockState.getBlock() instanceof BedBlock) {
                if (blockState.getValue(BedBlock.PART) == BedPart.FOOT) {
                    Direction direction = blockState.getValue(BedBlock.FACING);
                    blockPos = blockPos.relative(direction);
                }
            } else if (blockState.getBlock() instanceof DoorBlock) {
                if (blockState.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
                    blockPos = blockPos.below();
                }
            }
        }
        serverLevel.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
    }

    public BlockPos getBlockPos() {
        return new BlockPos(x, y, z);
    }
}
