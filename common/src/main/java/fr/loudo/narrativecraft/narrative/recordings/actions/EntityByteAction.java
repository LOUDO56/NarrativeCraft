package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.mixin.fields.EntityFields;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import net.minecraft.network.syncher.SynchedEntityData;

public class EntityByteAction extends Action {

    private final byte entityByte;
    private final byte previousEntityByte;

    public EntityByteAction(int waitTick, byte entityByte, byte previousEntityByte) {
        super(waitTick, ActionType.ENTITY_BYTE);
        this.entityByte = entityByte;
        this.previousEntityByte = previousEntityByte;
    }

    @Override
    public void execute(Playback.PlaybackData playbackData) {
        SynchedEntityData entityData = playbackData.getEntity().getEntityData();
        entityData.set(EntityFields.getDATA_SHARED_FLAGS_ID(), entityByte);
    }

    @Override
    public void rewind(Playback.PlaybackData playbackData) {
        SynchedEntityData entityData = playbackData.getEntity().getEntityData();
        entityData.set(EntityFields.getDATA_SHARED_FLAGS_ID(), previousEntityByte);
    }
}
