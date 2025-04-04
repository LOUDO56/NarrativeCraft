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

    public static void breakBlock(BlockPos blockPos, ServerPlayer serverPlayer) {
        if (NarrativeCraftMod.getInstance().getRecordingHandler().isPlayerRecording(serverPlayer)) {
            Recording recording = NarrativeCraftMod.getInstance().getRecordingHandler().getRecordingOfPlayer(serverPlayer);
            boolean blockDropped = serverPlayer.gameMode().isSurvival();
            BreakBlockAction breakBlockAction = new BreakBlockAction(recording.getTickAction(), ActionType.BLOCK_BREAK, blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockDropped);
            recording.getActionsData().addAction(breakBlockAction);
        }

    }

}
