package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.actions.PlaceBlockAction;
import net.minecraft.core.BlockPos;
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
            PlaceBlockAction placeBlockAction = new PlaceBlockAction(recording.getTick(), blockPos, blockState);
            recording.getActionDataFromEntity(serverPlayer).addAction(placeBlockAction);
            if(blockState.getBlock() instanceof BedBlock) {
                BlockPos headPos = blockPos.relative(blockState.getValue(BedBlock.FACING));
                blockState = blockState.setValue(BedBlock.PART, BedPart.HEAD);
                PlaceBlockAction headBedplaceBlockAction = new PlaceBlockAction(recording.getTick(), headPos, blockState);
                recording.getActionDataFromEntity(serverPlayer).addAction(headBedplaceBlockAction);
            } else if (blockState.getBlock() instanceof DoorBlock) {
                BlockPos upperPos = blockPos.above();
                blockState = blockState.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
                PlaceBlockAction upperDoorPlaceAction = new PlaceBlockAction(recording.getTick(), upperPos, blockState);
                recording.getActionDataFromEntity(serverPlayer).addAction(upperDoorPlaceAction);
            }
        }
    }
}
