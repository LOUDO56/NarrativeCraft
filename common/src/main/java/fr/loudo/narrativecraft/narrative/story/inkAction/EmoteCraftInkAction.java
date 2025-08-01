package fr.loudo.narrativecraft.narrative.story.inkAction;

import com.zigythebird.playeranimcore.animation.Animation;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.inkAction.enums.InkTagType;
import fr.loudo.narrativecraft.narrative.story.inkAction.validation.ErrorLine;
import fr.loudo.narrativecraft.utils.Translation;
import io.github.kosmx.emotes.api.events.server.ServerEmoteAPI;
import io.github.kosmx.emotes.common.tools.UUIDMap;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import net.minecraft.client.Minecraft;

import java.util.UUID;

public class EmoteCraftInkAction extends InkAction {

    private String characterName;
    private boolean forced;
    private Animation emote;
    private String action;

    public EmoteCraftInkAction(StoryHandler storyHandler, String command) {
        super(storyHandler, InkTagType.EMOTE, command);
    }

    @Override
    public InkActionResult execute() {
        if(command.length < 3) return InkActionResult.error(this.getClass(),  Translation.message("validation.missing_values").getString());
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
        if(characterStory == null) return InkActionResult.error(this.getClass(), Translation.message("validation.character", characterName).getString());
        if(action.equals("play")) {
            emote = getEmote(name, UniversalEmoteSerializer.getLoadedEmotes());
            if(emote == null) return InkActionResult.error(this.getClass(), Translation.message("validation.emote", name).getString());
            ServerEmoteAPI.playEmote(characterStory.getEntity().getUUID(), emote, forced);
        } else if(action.equals("stop")) {
            ServerEmoteAPI.playEmote(characterStory.getEntity().getUUID(), null, false);
        }
        sendDebugDetails();
        return InkActionResult.pass();
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
                lineText,
                false
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
                lineText,
                false
        );
        if(command[1].equals("play")) {
            emote = getEmote(name, UniversalEmoteSerializer.getLoadedEmotes());
            if(emote == null) {
                return new ErrorLine(
                        line,
                        scene,
                        Translation.message("validation.emote", name).getString(),
                        lineText,
                        false
                );
            }
        }
        return null;
    }

    private Animation getEmote(String id, UUIDMap<Animation> emotes) {
        try {
            UUID emoteID = UUID.fromString(id);
            for (Animation keyframeAnimation : emotes) {
                if(keyframeAnimation.get().equals(emoteID)) {
                    return keyframeAnimation;
                }
            }
        } catch(IllegalArgumentException ignore) {} //Not a UUID

        for (Animation animation : emotes) {
            String emoteName = animation.data().name();
            emoteName = emoteName.replace("\"", "");
            if(emoteName.equalsIgnoreCase(id)) {
                return animation;
            }
        }
        return null;
    }
}
