package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.actions.PlaceBlockAction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class OnPlaceBlock {

    public static void placeBlock(BlockState blockState, BlockPos blockPos, ServerPlayer serverPlayer) {

        if (NarrativeCraftMod.getInstance().getRecordingHandler().isPlayerRecording(serverPlayer)) {
            Recording recording = NarrativeCraftMod.getInstance().getRecordingHandler().getRecordingOfPlayer(serverPlayer);
            int tick = recording.getActionDifference().getTick();
            String data = NbtUtils.writeBlockState(blockState).toString();
            PlaceBlockAction placeBlockAction = new PlaceBlockAction(tick, blockPos.getX(), blockPos.getY(), blockPos.getZ(), data);
            recording.getActionsData().addAction(placeBlockAction);
            if(blockState.getBlock() instanceof BedBlock) {
                BlockPos headPos = blockPos.relative(blockState.getValue(BedBlock.FACING));
                blockState = blockState.setValue(BedBlock.PART, BedPart.HEAD);
                data = NbtUtils.writeBlockState(blockState).toString();
                PlaceBlockAction headBedplaceBlockAction = new PlaceBlockAction(tick, headPos.getX(), headPos.getY(), headPos.getZ(), data);
                recording.getActionsData().addAction(headBedplaceBlockAction);
            } else if (blockState.getBlock() instanceof DoorBlock) {
                BlockPos upperPos = blockPos.above();
                blockState = blockState.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
                data = NbtUtils.writeBlockState(blockState).toString();
                PlaceBlockAction upperDoorPlaceAction = new PlaceBlockAction(tick, upperPos.getX(), upperPos.getY(), upperPos.getZ(), data);
                recording.getActionsData().addAction(upperDoorPlaceAction);
            }
        }


    }

}
