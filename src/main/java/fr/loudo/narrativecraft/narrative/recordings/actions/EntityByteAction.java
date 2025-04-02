package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;

public class EntityByteAction extends Action {

    private byte entityByte;

    public EntityByteAction(int waitTick, ActionType actionType, byte entityByte) {
        super(waitTick, actionType);
        this.entityByte = entityByte;
    }

    public byte getEntityByte() {
        return entityByte;
    }
}
