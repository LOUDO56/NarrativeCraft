package fr.loudo.narrativecraft.narrative.story.inkAction;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.utils.Translation;
import io.github.kosmx.emotes.api.events.server.ServerEmoteAPI;
import io.github.kosmx.emotes.mc.McUtils;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.StringUtil;

import java.util.HashMap;
import java.util.UUID;

public class EmoteCraftInkAction extends InkAction {

    private String characterName;
    private boolean forced;
    private KeyframeAnimation emote;
    private String action;

    public EmoteCraftInkAction() {}

    public EmoteCraftInkAction(StoryHandler storyHandler) {
        super(storyHandler);
    }

    @Override
    public InkActionResult execute(String[] command) {
        if(command.length < 3) return InkActionResult.ERROR;
        forced = false;
        characterName = InkAction.parseName(command, 2);
        name = "";
        action = command[1];
        if(action.equals("play")) {
            name = InkAction.parseName(command, 3);
        }
        try {
            forced = Boolean.parseBoolean(command[command.length - 1]);
        } catch (RuntimeException ignored) {}
        CharacterStory characterStory = storyHandler.getCharacter(characterName);
        if(characterStory == null) return InkActionResult.ERROR;
        if(action.equals("play")) {
            emote = getEmote(name, UniversalEmoteSerializer.getLoadedEmotes());
            if(emote == null) return InkActionResult.ERROR;
            ServerEmoteAPI.playEmote(characterStory.getEntity().getUUID(), emote, forced);
        } else if(action.equals("stop")) {
            ServerEmoteAPI.playEmote(characterStory.getEntity().getUUID(), null, false);
        }
        sendDebugDetails();
        return InkActionResult.PASS;
    }

    @Override
    void sendDebugDetails() {
        if(storyHandler.isDebugMode()) {
            Minecraft.getInstance().player.displayClientMessage(
                    Translation.message("debug.emote", action, characterName, name, forced),
                    false
            );
        }
    }

    @Override
    public ErrorLine validate(String[] command, int line, String lineText, Scene scene) {
        if(command.length < 3) return new ErrorLine(
                line,
                scene,
                Translation.message("validation.missing_values").getString(),
                lineText
        );
        forced = false;
        characterName = InkAction.parseName(command, 2);
        name = "";
        if(command[1].equals("play")) {
            name = InkAction.parseName(command, 3);
        }
        try {
            forced = Boolean.parseBoolean(command[command.length - 1]);
        } catch (RuntimeException ignored) {}
        CharacterStory characterStory = NarrativeCraftMod.getInstance().getCharacterManager().getCharacter(characterName);
        if(characterStory == null) return new ErrorLine(
                line,
                scene,
                Translation.message("validation.character", characterName).getString(),
                lineText
        );
        if(command[1].equals("play")) {
            emote = getEmote(name, UniversalEmoteSerializer.getLoadedEmotes());
            if(emote == null) {
                return new ErrorLine(
                        line,
                        scene,
                        Translation.message("validation.emote", name).getString(),
                        lineText
                );
            }
        }
        return null;
    }

    private KeyframeAnimation getEmote(String id, HashMap<UUID, KeyframeAnimation> emotes) {
        // Comes from https://github.com/KosmX/emotes/blob/73f9a2bfdf041504738d1d2f89b67bd6525e450c/emotesMc/src/main/java/io/github/kosmx/emotes/mc/EmoteArgumentProvider.java#L56
        try {
            UUID emoteID = UUID.fromString(id);
            return emotes.get(emoteID);
        } catch(IllegalArgumentException ignore) {} //Not a UUID

        for (var emote : emotes.values()) {
            if (emote.extraData.containsKey("name")) {
                String name = StringUtil.filterText(McUtils.fromJson(emote.extraData.get("name"), RegistryAccess.EMPTY).getString());
                if (name.equals(id)) return emote;
            }
        }
        return null;
    }
}
