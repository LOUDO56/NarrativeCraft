package fr.loudo.narrativecraft.narrative.recordings.actions.manager;

import com.google.gson.*;
import fr.loudo.narrativecraft.narrative.recordings.actions.Action;

import java.lang.reflect.Type;

public class ActionGsonParser implements JsonDeserializer<Action> {

    @Override
    public Action deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        JsonElement actionTypElem = jsonObject.get("actionType");
        if(actionTypElem == null) return null;
        ActionType actionType = ActionType.valueOf(actionTypElem.getAsString());
        return context.deserialize(json, actionType.getActionClass());

    }
}
