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
        int tick = jsonObject.get("tick").getAsInt();

        switch (actionType) {
            case SWING -> {
                InteractionHand interactionHand = InteractionHand.valueOf(jsonObject.get("interactionHand").getAsString());
                return new SwingAction(tick, actionType, interactionHand);
            }
            case POSE -> {
                Pose pose = Pose.valueOf(jsonObject.get("pose").getAsString());
                return new PoseAction(tick, actionType, pose);
            }
            case ENTITY_BYTE -> {
                byte entityByte = jsonObject.get("entityByte").getAsByte();
                return new EntityByteAction(tick, actionType, entityByte);
            }
            case LIVING_ENTITY_BYTE -> {
                byte livingEntityByte = jsonObject.get("entityByte").getAsByte();
                return new LivingEntityByteAction(tick, actionType, livingEntityByte);
            }
            case HURT -> {
                return new HurtAction(tick, actionType);
            }
            case ITEM_CHANGE -> {
                String equipmentSlot = jsonObject.get("equipmentSlot").getAsString();
                int itemId = jsonObject.get("itemId").getAsInt();
                JsonElement data = jsonObject.get("data");
                if(data != null) {
                    return new ItemChangeAction(tick, actionType, itemId, equipmentSlot, data.getAsString());
                } else {
                    return new ItemChangeAction(tick, actionType, equipmentSlot, itemId);
                }
            }
            case BLOCK_PLACE -> {
                int x = jsonObject.get("x").getAsInt();
                int y = jsonObject.get("y").getAsInt();
                int z = jsonObject.get("z").getAsInt();
                String data = jsonObject.get("data").getAsString();
                return new PlaceBlockAction(tick, actionType, x, y, z, data);
            }
            case BLOCK_BREAK -> {
                int x = jsonObject.get("x").getAsInt();
                int y = jsonObject.get("y").getAsInt();
                int z = jsonObject.get("z").getAsInt();
                boolean drop = jsonObject.get("drop").getAsBoolean();
                return new BreakBlockAction(tick, actionType, x, y, z, drop);
            }

        }

        return new Action(tick, actionType);

    }
}
