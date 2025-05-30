package fr.loudo.narrativecraft.narrative.story.inkAction;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

public abstract class InkAction {

    protected String name;
    protected StoryHandler storyHandler;

    public InkAction() {}

    public InkAction(StoryHandler storyHandler) {
        this.storyHandler = storyHandler;
        this.name = "";
    }

    public abstract boolean execute(String[] command);
    abstract void sendDebugDetails();
    public abstract ErrorLine validate(String[] command, int line, String lineText, Scene scene);

    public String getName() {
        return name;
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
        } else {
            return null;
        }
    }

    public enum InkTagType {
        ON_ENTER,
        CUTSCENE,
        CAMERA_ANGLE,
        FADE,
        SONG_SFX_START,
        SONG_SFX_STOP,
        SOUND_STOP_ALL,
        WAIT
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
            this.fileName = scene.getCamelCase() + NarrativeCraftFile.EXTENSION_SCRIPT_FILE;
            this.message = message;
            this.lineText = lineText;
        }

        public Component toMessage() {
            return Component.empty()
                    .append("\n")
                    .append(Component.literal("Chapter: ")
                            .withColor(0xF24949)
                            .withStyle(ChatFormatting.BOLD)
                    )
                    .append(Component.literal(String.valueOf(scene.getChapter().getIndex()))
                            .withColor(0xF24949)
                            .withStyle(style -> style.withBold(false))
                    )
                    .append("\n")
                    .append(Component.literal("Scene: ")
                            .withColor(0xF24949)
                            .withStyle(ChatFormatting.BOLD)
                    )
                    .append(Component.literal(scene.getName() + " ")
                            .withColor(0xF24949)
                            .withStyle(style -> style.withBold(false))
                    )
                    .append(Component.literal("(" + fileName + ")")
                            .withColor(0xb0b0b0)
                            .withStyle(style -> style.withBold(false)
                                    .withClickEvent(new ClickEvent.OpenFile(
                                            NarrativeCraftFile.getSceneFile(scene)
                                    ))
                                    .withHoverEvent(new HoverEvent.ShowText(Translation.message("validation.quick_edit")))
                            )
                    )
                    .append("\n")
                    .append(Translation.message("global.line")
                            .withColor(0xC97C08)
                    )
                    .append(" " + line + ": ")
                    .withColor(0xC97C08)
                    .append(Component.literal(lineText)
                            .withColor(0xb0b0b0)
                            .withStyle(style -> style.withBold(false))
                    )
                    .append("\n")
                    .append(Component.literal("'" + message + "'")
                            .withColor(0xF24949)
                            .withStyle(style -> style.withBold(false))
                    )
                    .append("\n");
        }


    }


}
