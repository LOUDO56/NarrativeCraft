package fr.loudo.narrativecraft.narrative.recordings.actions.manager;

import com.google.gson.*;
import fr.loudo.narrativecraft.narrative.recordings.actions.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Pose;

import java.lang.reflect.Type;

public class ActionDeserializer implements JsonDeserializer<Action> {

    @Override
    public Action deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        ActionType actionType = ActionType.valueOf(jsonObject.get("actionType").getAsString());
        int waitTick = jsonObject.get("tick").getAsInt();

        switch (actionType) {
            case SWING -> {
                InteractionHand interactionHand = InteractionHand.valueOf(jsonObject.get("interactionHand").getAsString());
                return new SwingAction(waitTick, actionType, interactionHand);
            }
            case POSE -> {
                Pose pose = Pose.valueOf(jsonObject.get("pose").getAsString());
                return new PoseAction(waitTick, actionType, pose);
            }
            case ENTITY_BYTE -> {
                byte entityByte = jsonObject.get("entityByte").getAsByte();
                return new EntityByteAction(waitTick, actionType, entityByte);
            }
            case HURT -> {
                return new HurtAction(waitTick, actionType);
            }
            case ITEM_CHANGE -> {
                int itemId = jsonObject.get("itemId").getAsInt();
                return new ItemChangeAction(waitTick, actionType, itemId);
            }
//            case ITEM_USED -> {
//                InteractionHand interactionHand = InteractionHand.valueOf(jsonObject.get("interactionHand").getAsString());
//                return new ItemUsedAction(waitTick, actionType, interactionHand);
//            }

        }

        return new Action(waitTick, actionType);

    }
}
