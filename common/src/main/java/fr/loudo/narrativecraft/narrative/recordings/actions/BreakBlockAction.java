package fr.loudo.narrativecraft.narrative.recordings.actions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

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
        BlockPos blockPos = new BlockPos(x, y, z);
        ServerLevel serverLevel = Utils.getServerLevel();
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
        serverLevel.destroyBlock(blockPos, drop);
    }

    @Override
    public void rewind(LivingEntity entity) {
        ServerLevel serverLevel = Utils.getServerLevel();
        BlockState blockState = Utils.getBlockStateFromData(data, entity.registryAccess());
        if(blockState == null) return;
        BlockPos blockPos = new BlockPos(x, y, z);
        Block block = blockState.getBlock();
        if(block instanceof DoorBlock) {
            if (blockState.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
                blockState = blockState.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
                blockPos = blockPos.below();
            }
        } else if (blockState.getBlock() instanceof BedBlock) {
            if (blockState.getValue(BedBlock.PART) == BedPart.HEAD) {
                Direction direction = blockState.getValue(BedBlock.FACING);
                blockState = blockState.setValue(BedBlock.PART, BedPart.FOOT);
                blockPos = blockPos.relative(direction.getOpposite());
            }
        }
        serverLevel.setBlock(blockPos, blockState, 3);
        if(block instanceof BedBlock || block instanceof DoorBlock) {
            block.setPlacedBy(entity.level(), blockPos, blockState, entity, blockState.getBlock().asItem().getDefaultInstance());
        }
    }
}
