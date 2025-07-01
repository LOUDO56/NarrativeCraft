package fr.loudo.narrativecraft.narrative.story.inkAction.manager;

import com.google.gson.*;
import fr.loudo.narrativecraft.narrative.story.inkAction.*;

import java.lang.reflect.Type;

public class InkActionSerializer implements JsonDeserializer<InkAction> {

    @Override
    public InkAction deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        if(jsonObject.get("inkTagType") == null) return null;
        String tagTypeString = jsonObject.get("inkTagType").getAsString();
        InkAction.InkTagType inkTagType = InkAction.InkTagType.valueOf(tagTypeString);
        switch (inkTagType) {
            case DAYTIME -> {
                return context.deserialize(json, ChangeDayTimeInkAction.class);
            }
            case FADE -> {
                return context.deserialize(json, FadeScreenInkAction.class);
            }
            case SHAKE -> {
                return context.deserialize(json, ShakeScreenInkAction.class);
            }
            case SONG_SFX_START, SONG_SFX_STOP -> {
                return context.deserialize(json, SongSfxInkAction.class);
            }
            case WAIT -> {
                return context.deserialize(json, WaitInkAction.class);
            }
        }
        return null;
    }

}
