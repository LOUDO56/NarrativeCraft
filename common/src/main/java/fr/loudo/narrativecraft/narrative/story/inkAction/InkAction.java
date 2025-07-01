package fr.loudo.narrativecraft.narrative.story.inkAction;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import org.jetbrains.annotations.Nullable;

public abstract class InkAction {

    protected String name;
    protected InkTagType inkTagType;
    protected transient StoryHandler storyHandler;

    public InkAction() {}

    public InkAction(StoryHandler storyHandler, InkTagType inkTagType) {
        this.storyHandler = storyHandler;
        this.name = "";
        this.inkTagType = inkTagType;
    }

    public abstract InkActionResult execute(String[] command);
    abstract void sendDebugDetails();
    public abstract ErrorLine validate(String[] command, int line, String lineText, Scene scene);

    public String getName() {
        return name;
    }

    public StoryHandler getStoryHandler() {
        return storyHandler;
    }

    public void setStoryHandler(StoryHandler storyHandler) {
        this.storyHandler = storyHandler;
    }

    public static InkTagType getInkActionTypeByTag(String tag) {
        if(tag.contains("on enter")) {
            return InkTagType.ON_ENTER;
        } else if(tag.contains("cutscene start")) {
            return InkTagType.CUTSCENE;
        } else if(tag.contains("camera set")) {
            return InkTagType.CAMERA_ANGLE;
        } else if (tag.contains("song start") || tag.contains("sfx start")) {
            return InkTagType.SONG_SFX_START;
        } else if (tag.contains("song stop") || tag.contains("sfx stop")) {
            return InkTagType.SONG_SFX_STOP;
        } else if (tag.contains("sound stop all")) {
            return InkTagType.SOUND_STOP_ALL;
        } else if (tag.contains("fade")) {
            return InkTagType.FADE;
        } else if (tag.contains("wait")) {
            return InkTagType.WAIT;
        } else if (tag.equalsIgnoreCase("save")) {
            return InkTagType.SAVE;
        } else if (tag.contains("subscene start") || tag.contains("subscene stop")) {
            return InkTagType.SUBSCENE;
        } else if (tag.contains("animation start") || tag.contains("animation stop")) {
            return InkTagType.ANIMATION;
        } else if (tag.contains("time add") || tag.contains("time set")) {
            return InkTagType.DAYTIME;
        } else if (tag.contains("weather")) {
            return InkTagType.WEATHER;
        } else if (tag.contains("command")) {
            return InkTagType.MINECRAFT_COMMAND;
        } else if (tag.contains("dialog")) {
            return InkTagType.DIALOG_VALUES;
        } else if (tag.contains("shake")) {
            return InkTagType.SHAKE;
        } else if (tag.contains("emote play") || tag.contains("emote stop")) {
            return InkTagType.EMOTE;
        } else if (tag.startsWith("kill")) {
            return InkTagType.KILL_CHARACTER;
        } else {
            return null;
        }
    }

    public static String parseName(String[] command, int index) {
        String name = command[index];
        if (name.startsWith("\"")) {
            StringBuilder builder = new StringBuilder();
            for (int i = index; i < command.length; i++) {
                builder.append(command[i]);
                if(command[i].endsWith("\"")) break;
                if (i < command.length - 1) builder.append(" ");
            }
            name = builder.toString();
            if (name.startsWith("\"") && name.endsWith("\"") && name.length() >= 2) {
                name = name.substring(1, name.length() - 1);
            }
        }
        return name;
    }

    public static @Nullable InkAction getInkAction(InkAction.InkTagType tagType) {
        InkAction inkAction = null;
        switch (tagType) {
            case CUTSCENE -> inkAction = new CutsceneInkAction();
            case CAMERA_ANGLE ->  inkAction = new CameraAngleInkAction();
            case SONG_SFX_START, SONG_SFX_STOP, SOUND_STOP_ALL -> inkAction = new SongSfxInkAction();
            case FADE -> inkAction = new FadeScreenInkAction();
            case WAIT -> inkAction = new WaitInkAction();
            case SUBSCENE -> inkAction = new SubscenePlayInkAction();
            case ANIMATION -> inkAction = new AnimationPlayInkAction();
            case DAYTIME -> inkAction = new ChangeDayTimeInkAction();
            case WEATHER -> inkAction = new WeatherChangeInkAction();
            case MINECRAFT_COMMAND -> inkAction = new CommandMinecraftInkAction();
            case DIALOG_VALUES -> inkAction = new DialogValuesInkAction();
            case SHAKE -> inkAction = new ShakeScreenInkAction();
            case EMOTE -> inkAction = new EmoteCraftInkAction();
            case KILL_CHARACTER -> inkAction = new KillCharacterInkAction();
        }
        return inkAction;
    }

    public enum InkTagType {
        ON_ENTER,
        CUTSCENE,
        CAMERA_ANGLE,
        FADE,
        SONG_SFX_START,
        SONG_SFX_STOP,
        SOUND_STOP_ALL,
        WAIT,
        SAVE,
        SUBSCENE,
        ANIMATION,
        DAYTIME,
        WEATHER,
        MINECRAFT_COMMAND,
        DIALOG_VALUES,
        SHAKE,
        EMOTE,
        KILL_CHARACTER
    }

    public enum InkActionResult {
        PASS,
        BLOCK,
        ERROR
    }

    public static class ErrorLine {
        private final int line;
        private final Scene scene;
        private final String fileName;
        private final String message;
        private final String lineText;

        public ErrorLine(int line, Scene scene, String message, String lineText) {
            this.line = line;
            this.scene = scene;
            this.fileName = scene.getSnakeCase() + NarrativeCraftFile.EXTENSION_SCRIPT_FILE;
            this.message = message;
            this.lineText = lineText;
        }

        public Component toMessage() {
            return Component.empty()
                    .append("\n")
                    .append(Component.literal("Chapter: ")
                            .withColor(ChatFormatting.RED.getColor())
                            .withStyle(ChatFormatting.BOLD)
                    )
                    .append(Component.literal(String.valueOf(scene.getChapter().getIndex()))
                            .withColor(ChatFormatting.RED.getColor())
                            .withStyle(style -> style.withBold(false))
                    )
                    .append("\n")
                    .append(Component.literal("Scene: ")
                            .withColor(ChatFormatting.RED.getColor())
                            .withStyle(ChatFormatting.BOLD)
                    )
                    .append(Component.literal(scene.getName() + " ")
                            .withColor(ChatFormatting.RED.getColor())
                            .withStyle(style -> style.withBold(false))
                    )
                    .append(Component.literal("(" + fileName + ")")
                            .withColor(ChatFormatting.GRAY.getColor())
                            .withStyle(style -> style.withBold(false)
                                    .withClickEvent(new ClickEvent.OpenFile(
                                            NarrativeCraftFile.getSceneInkFile(scene)
                                    ))
                                    .withHoverEvent(new HoverEvent.ShowText(Translation.message("validation.quick_edit")))
                            )
                    )
                    .append("\n")
                    .append(Translation.message("global.line")
                            .withColor(ChatFormatting.GOLD.getColor())
                    )
                    .append(" " + line + ": ")
                    .withColor(ChatFormatting.GOLD.getColor())
                    .append(Component.literal(lineText)
                            .withColor(ChatFormatting.GRAY.getColor())
                            .withStyle(style -> style.withBold(false))
                    )
                    .append("\n")
                    .append(Component.literal("'" + message + "'")
                            .withColor(ChatFormatting.RED.getColor())
                            .withStyle(style -> style.withBold(false))
                    )
                    .append("\n");
        }

        public int getLine() {
            return line;
        }

        public Scene getScene() {
            return scene;
        }

        public String getFileName() {
            return fileName;
        }

        public String getMessage() {
            return message;
        }

        public String getLineText() {
            return lineText;
        }
    }


}
