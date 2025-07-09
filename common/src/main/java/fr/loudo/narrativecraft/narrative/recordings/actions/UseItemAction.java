package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.utils.FakePlayer;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

public class UseItemAction extends Action {

    private final String handName;

    public UseItemAction(int tick, InteractionHand interactionHand) {
        super(tick, ActionType.USE_ITEM);
        handName = interactionHand.name();
    }

    @Override
    public void execute(Playback.PlaybackData playbackData) {
        if(playbackData.getEntity() instanceof FakePlayer fakePlayer) {
            ServerLevel serverLevel = Utils.getServerLevel();
            ItemStack itemStack = fakePlayer.getItemInHand(InteractionHand.valueOf(handName));
            if(itemStack.getItem() instanceof SpawnEggItem || itemStack.getItem() instanceof BoatItem) return;
            itemStack.setCount(2);
            itemStack.getItem().use(serverLevel, fakePlayer, InteractionHand.valueOf(handName));
        }
    }

    @Override
    public void rewind(Playback.PlaybackData playbackData) {}
}
