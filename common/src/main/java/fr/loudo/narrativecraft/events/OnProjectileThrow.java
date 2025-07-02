package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.actions.ProjectileThrowAction;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class OnProjectileThrow {

    // Standby
    public static void projectileThrow(ItemStack itemStack, InteractionHand hand, ServerPlayer serverPlayer) {
        if (NarrativeCraftMod.getInstance().getRecordingHandler().isPlayerRecording(serverPlayer)) {
            Recording recording = NarrativeCraftMod.getInstance().getRecordingHandler().getRecordingOfPlayer(serverPlayer);
            int itemId = Item.getId(itemStack.getItem());
            ProjectileThrowAction projectileThrowAction = new ProjectileThrowAction(recording.getActionDifference().getTick(), itemId, hand.name());
            recording.getActionsData().addAction(projectileThrowAction);
        }
    }

}
