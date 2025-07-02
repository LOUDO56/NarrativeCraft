package fr.loudo.narrativecraft.narrative.recordings.actions.manager;

import com.google.gson.*;
import fr.loudo.narrativecraft.narrative.recordings.actions.*;

import java.lang.reflect.Type;

public class ActionGsonParser implements JsonDeserializer<Action>, JsonSerializer<Action> {

    private static final String CLASS_META_KEY = "_class";

    @Override
    public Action deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String className = jsonObject.get(CLASS_META_KEY).getAsString();
        try {
            Class<?> clazz = Class.forName(className);
            return context.deserialize(json, clazz);
        } catch (ClassNotFoundException e) {
            return null;
        }

    }

    @Override
    public JsonElement serialize(Action src, Type typeOfSrc, JsonSerializationContext context) {
        JsonElement jsonElement = context.serialize(src, src.getClass());
        jsonElement.getAsJsonObject().addProperty(CLASS_META_KEY, src.getClass().getCanonicalName());
        return jsonElement;
    }
}
