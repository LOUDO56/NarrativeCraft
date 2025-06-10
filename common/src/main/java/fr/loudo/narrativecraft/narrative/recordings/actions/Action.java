package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public class Action {

    private int tick;
    private ActionType actionType;

    public Action(int tick, ActionType actionType) {
        this.tick = tick;
        this.actionType = actionType;
    }

    public int getTick() {
        return tick;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void execute(LivingEntity entity) {};

    public static void parseAndExecute(Action action, LivingEntity livingEntity) {
        if(action == null) return;
        ServerLevel serverLevel = NarrativeCraftMod.server.getLevel(Minecraft.getInstance().level.dimension());
        switch (action) {
            case PlaceBlockAction placeBlockAction -> placeBlockAction.execute(livingEntity, serverLevel);
            case BreakBlockAction breakBlockAction -> breakBlockAction.execute(serverLevel);
            case DestroyBlockStageAction destroyBlockStageAction -> destroyBlockStageAction.execute(serverLevel);
            case RightClickBlockAction rightClickBlockAction -> rightClickBlockAction.execute(livingEntity);
            default -> action.execute(livingEntity);
        }
    }
}
