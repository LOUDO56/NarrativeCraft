package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.MovementData;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.utils.FakePlayer;
import net.minecraft.world.entity.item.ItemEntity;

public class ItemPickUpAction extends Action {

    private final int entityRecordingId;

    public ItemPickUpAction(int tick, int entityRecordingId) {
        super(tick, ActionType.ITEM_PICKUP);
        this.entityRecordingId = entityRecordingId;
    }

    @Override
    public void execute(Playback.PlaybackData playbackData) {
        if(playbackData.getEntity() instanceof FakePlayer fakePlayer) {
            ItemEntity item = (ItemEntity) playbackData.getPlayback().getEntityByRecordId(entityRecordingId);
            if(item == null) return;
            fakePlayer.take(item, item.getItem().getCount());
        }
    }

    @Override
    public void rewind(Playback.PlaybackData playbackData) {
        Playback.PlaybackData itemPlayback = playbackData.getPlayback().getPlaybackDataByRecordId(entityRecordingId);
        itemPlayback.killEntity();
        MovementData takeLoc = playbackData.getActionsData().getMovementData().get(tick);
        if(takeLoc == null) return;
        itemPlayback.spawnEntity(takeLoc);
    }

}
