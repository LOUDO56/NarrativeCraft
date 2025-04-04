package fr.loudo.narrativecraft.narrative.recordings.actions.manager;

import com.google.gson.*;
import fr.loudo.narrativecraft.narrative.recordings.actions.*;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
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
            case LIVING_ENTITY_BYTE -> {
                byte livingEntityByte = jsonObject.get("entityByte").getAsByte();
                return new LivingEntityByteAction(waitTick, actionType, livingEntityByte);
            }
            case HURT -> {
                return new HurtAction(waitTick, actionType);
            }
            case ITEM_CHANGE -> {
                String equipmentSlot = jsonObject.get("equipmentSlot").getAsString();
                int itemId = jsonObject.get("itemId").getAsInt();
                JsonElement data = jsonObject.get("data");
                if(data != null) {
                    return new ItemChangeAction(waitTick, actionType, itemId, equipmentSlot, data.getAsString());
                } else {
                    return new ItemChangeAction(waitTick, actionType, equipmentSlot, itemId);
                }
            }

        }

        return new Action(waitTick, actionType);

    }
}
