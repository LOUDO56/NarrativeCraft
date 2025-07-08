package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.actions.RightClickBlockAction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

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
            recording.getActionDataFromEntity(serverPlayer).addAction(rightClickBlockAction);
        }
    }

}
