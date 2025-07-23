package fr.loudo.narrativecraft.narrative.story.inkAction.enums;

import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.inkAction.*;
import fr.loudo.narrativecraft.narrative.story.inkAction.interfaces.CommandMatcher;

import java.lang.reflect.Constructor;

public enum InkTagType {
    ON_ENTER(
            "on enter",
            line -> line.equals("on enter"),
            OnEnterInkAction.class
    ),
    CUTSCENE(
            "cutscene start %cutscene_name%",
            line -> line.startsWith("cutscene start"),
            CutsceneInkAction.class
    ),
    CAMERA_ANGLE(
            "camera set %parent% %child%",
            line -> line.startsWith("camera set"),
            CameraAngleInkAction.class
    ),
    FADE(
            "fade %fadeInValue% %stayValue% %fadeOutValue% %color%",
            line -> line.startsWith("fade"),
            FadeScreenInkAction.class
    ),
    SONG_SFX_START(
            "<song|sfx> start %namespace:category.name% [%volume% %pitch% [loop=true/false] [fadein %fadeTime%]]",
            line -> line.startsWith("song start") || line.startsWith("sfx start"),
            SongSfxInkAction.class
    ),
    SONG_SFX_STOP(
            "<song|sfx> stop %namespace:category.name% [fadeout %fadeTime%]",
            line -> line.startsWith("song stop") || line.startsWith("sfx stop"),
            SongSfxInkAction.class
    ),
    SOUND_STOP_ALL(
            "sound stop all",
            line -> line.equals("sound stop all"),
            SongSfxInkAction.class
    ),
    COOLDOWN(
            "wait %time% <second(s), minute(s), hour(s)>",
            line -> line.startsWith("wait"),
            CooldownInkAction.class
    ),
    SAVE(
            "save",
            line -> line.equals("save"),
            SaveInkAction.class
    ),
    SUBSCENE(
          "subscene start %subscene_name% [loop=true/false] [unique=true/false] [block=true/false]",
          line -> line.startsWith("subscene start"),
          SubscenePlayInkAction.class
    ),
    ANIMATION(
            "animation start %animation_name% [loop=true/false] [unique=true/false] [block=true/false]",
            line -> line.startsWith("animation start"),
            AnimationPlayInkAction.class
    ),
    DAYTIME(
            "time <set,add> <day,midnight,night,noon,%tick%> [to <day,midnight,night,noon,%tick%> for %time% <second(s), minute(s), hour(s)> [%easing%]]",
            line -> line.startsWith("time set") || line.startsWith("time add"),
            ChangeDayTimeInkAction.class
    ),
    WEATHER(
            "weather set <clear, rain, thunder>",
            line -> line.startsWith("weather set"),
            WeatherChangeInkAction.class
    ),
    MINECRAFT_COMMAND(
            "command %command_value%",
            line -> line.startsWith("command"),
            CommandMinecraftInkAction.class
    ),
    DIALOG_VALUES(
            "dialog <offset, scale, padding, width, textColor, backgroundColor, gap, letterSpacing, unSkippable, autoSkip, bobbing> [%value1%] [%value2%]",
            line -> line.startsWith("dialog"),
            DialogValuesInkAction.class
    ),
    SHAKE(
            "shake %strength% %decay_rate% %speed%",
            line -> line.startsWith("shake"),
            ShakeScreenInkAction.class
    ),
    EMOTE(
            "emote <play|stop> %emote_name% %character_name% %isForced%",
            line -> line.startsWith("emote play") || line.startsWith("emote stop"),
            EmoteCraftInkAction.class
    ),
    KILL_CHARACTER(
           "kill %name%",
           line -> line.startsWith("kill"),
           KillCharacterInkAction.class
    ),
    BORDER(
            "border %up% %right% %down% %left% [%color%] [%opacity%]",
            line -> line.startsWith("border"),
            BorderInkAction.class
    );

    private final String commandGrammar;
    private final Class<? extends InkAction> clazz;
    private final CommandMatcher matcher;

    InkTagType(String commandGrammar, CommandMatcher matcher, Class<? extends InkAction> clazz) {
        this.commandGrammar = commandGrammar;
        this.matcher = matcher;
        this.clazz = clazz;
    }

    public InkAction instantiate(StoryHandler storyHandler, String tag) throws Exception {
        Constructor<? extends InkAction> constructor = clazz.getDeclaredConstructor(StoryHandler.class, String.class);
        return constructor.newInstance(storyHandler, tag);
    }

    public InkAction getDefaultInstance() {
        try {
            Constructor<? extends InkAction> constructor = clazz.getDeclaredConstructor(StoryHandler.class, String.class);
            return constructor.newInstance(null, "");
        } catch (Exception e) {
            return null;
        }
    }

    public String getCommandGrammar() {
        return commandGrammar;
    }

    public Class<? extends InkAction> getClazz() {
        return clazz;
    }

    public CommandMatcher getMatcher() {
        return matcher;
    }

    public static InkTagType resolveType(String tag) {
        for (InkTagType inkTagType : InkTagType.values()) {
            if(inkTagType.matcher.matches(tag)) {
                return inkTagType;
            }
        }
        return null;
    }
}
