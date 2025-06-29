package fr.loudo.narrativecraft.narrative.recordings.actions.manager;

import com.google.gson.*;
import fr.loudo.narrativecraft.narrative.recordings.actions.*;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;

import java.lang.reflect.Type;
import java.util.UUID;

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
                Pose previousPose = null;
                if(jsonObject.get("previousPose") != null) {
                    previousPose = Pose.valueOf(jsonObject.get("previousPose").getAsString());
                }
                return new PoseAction(tick, actionType, pose, previousPose);
            }
            case ENTITY_BYTE -> {
                byte entityByte = jsonObject.get("entityByte").getAsByte();
                byte previousEntityByte = jsonObject.get("previousEntityByte").getAsByte();
                return new EntityByteAction(tick, actionType, entityByte, previousEntityByte);
            }
            case LIVING_ENTITY_BYTE -> {
                byte livingEntityByte = jsonObject.get("livingEntityByte").getAsByte();
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
                String data = jsonObject.get("data").getAsString();
                return new BreakBlockAction(tick, actionType, x, y, z, drop, data);
            }
            case DESTROY_BLOCK_STAGE -> {
                int x = jsonObject.get("x").getAsInt();
                int y = jsonObject.get("y").getAsInt();
                int z = jsonObject.get("z").getAsInt();
                int id = jsonObject.get("id").getAsInt();
                int progress = jsonObject.get("progress").getAsInt();
                return new DestroyBlockStageAction(tick, actionType, id, x, y, z, progress);
            }
            case RIGHT_CLICK_BLOCK -> {
                int x = jsonObject.get("x").getAsInt();
                int y = jsonObject.get("y").getAsInt();
                int z = jsonObject.get("z").getAsInt();
                String directionName = jsonObject.get("directionName").getAsString();
                String handName = jsonObject.get("handName").getAsString();
                boolean inside = jsonObject.get("inside").getAsBoolean();
                return new RightClickBlockAction(tick, actionType, x, y, z, directionName, handName, inside);
            }
            case EMOTE -> {
                JsonElement emoteIdElement = jsonObject.get("emoteId");
                UUID emoteId;
                if(emoteIdElement != null) {
                    emoteId = UUID.fromString(jsonObject.get("emoteId").getAsString());
                } else {
                    emoteId = null;
                }
                return new EmoteAction(tick, actionType, emoteId);
            }

        }

        return new Action(tick, actionType);

    }
}
