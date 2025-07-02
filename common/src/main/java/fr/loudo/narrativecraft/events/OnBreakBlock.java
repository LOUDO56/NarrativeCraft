package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.actions.BreakBlockAction;
import fr.loudo.narrativecraft.narrative.recordings.actions.PlaceBlockAction;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

public class OnBreakBlock {

    public static void breakBlock(BlockState blockState, BlockPos blockPos, ServerPlayer serverPlayer) {
        if (NarrativeCraftMod.getInstance().getRecordingHandler().isPlayerRecording(serverPlayer)) {
            Recording recording = NarrativeCraftMod.getInstance().getRecordingHandler().getRecordingOfPlayer(serverPlayer);
            boolean blockDropped = serverPlayer.gameMode().isSurvival();
            String data = NbtUtils.writeBlockState(blockState).toString();
            BreakBlockAction breakBlockAction = new BreakBlockAction(recording.getActionDifference().getTick(), blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockDropped, data);
            recording.getActionsData().addAction(breakBlockAction);
        }

    }

}
