package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.actions.RightClickBlockAction;
import fr.loudo.narrativecraft.narrative.recordings.actions.SleepAction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;

public class OnRightClickBlock {

    public static void onRightClick(Direction direction, BlockPos blockPos, InteractionHand hand, boolean inside, ServerPlayer serverPlayer) {
        if (NarrativeCraftMod.getInstance().getRecordingHandler().isPlayerRecording(serverPlayer)) {
            Recording recording = NarrativeCraftMod.getInstance().getRecordingHandler().getRecordingOfPlayer(serverPlayer);
            RightClickBlockAction rightClickBlockAction = new RightClickBlockAction(
                    recording.getTick(),
                    blockPos.getX(),
                    blockPos.getY(),
                    blockPos.getZ(),
                    direction.name(),
                    hand.name(),
                    inside
            );
            recording.getActionsData().addAction(rightClickBlockAction);
            BlockState blockState = serverPlayer.serverLevel().getBlockState(blockPos);
            if(blockState.getBlock() instanceof BedBlock) {
                SleepAction sleepAction = new SleepAction(recording.getTick(), blockPos);
                recording.getActionsData().addAction(sleepAction);
            }
        }
    }

}
