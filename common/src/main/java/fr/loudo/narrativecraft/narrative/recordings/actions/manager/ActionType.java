package fr.loudo.narrativecraft.narrative.recordings.actions.manager;

import fr.loudo.narrativecraft.narrative.recordings.actions.*;

public enum ActionType {
    SWING(SwingAction.class),
    POSE(PoseAction.class),
    ENTITY_BYTE(EntityByteAction.class),
    LIVING_ENTITY_BYTE(EntityByteAction.class),
    ITEM_CHANGE(ItemChangeAction.class),
    HURT(HurtAction.class),
    BLOCK_PLACE(PlaceBlockAction.class),
    BLOCK_BREAK(BreakBlockAction.class),
    DESTROY_BLOCK_STAGE(DestroyBlockStageAction.class),
    RIGHT_CLICK_BLOCK(RightClickBlockAction.class),
    EMOTE(EmoteAction.class),
    SLEEP(SleepAction.class),
    USE_ITEM(UseItemAction.class),
    GAMEMODE(GameModeAction.class),
    RIDE(RidingAction.class),
    ABSTRACT_HORSE_BYTE(AbstractHorseByteAction.class),
    ABSTRACT_BOAT_BUBBLE(AbstractBoatBubbleAction.class),
    ABSTRACT_BOAT_PADDLE(AbstractBoatPaddleAction.class),
    DEATH(DeathAction.class),
    RESPAWN(RespawnAction.class),
    ITEM_PICKUP(ItemPickUpAction.class);

    private final Class<? extends Action> actionClass;

    ActionType(Class<? extends Action> actionClass) {
        this.actionClass = actionClass;
    }

    public Class<? extends Action> getActionClass() {
        return actionClass;
    }
}
