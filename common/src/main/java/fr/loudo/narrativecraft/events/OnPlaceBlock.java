package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.actions.PlaceBlockAction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

public class OnPlaceBlock {

    public static void placeBlock(BlockState blockState, BlockPos blockPos, ServerPlayer serverPlayer) {

        if (NarrativeCraftMod.getInstance().getRecordingHandler().isPlayerRecording(serverPlayer)) {
            Recording recording = NarrativeCraftMod.getInstance().getRecordingHandler().getRecordingOfPlayer(serverPlayer);
            int tick = recording.getActionDifference().getTick();
            String data = NbtUtils.writeBlockState(blockState).toString();
            PlaceBlockAction placeBlockAction = new PlaceBlockAction(tick, blockPos.getX(), blockPos.getY(), blockPos.getZ(), data);
            recording.getActionsData().addAction(placeBlockAction);
        }


    }

}
