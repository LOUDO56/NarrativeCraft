package fr.loudo.narrativecraft.narrative.story.inkAction.validation;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

public class ErrorLine {
    private final boolean isWarn;
    private final int line;
    private final Scene scene;
    private String fileName;
    private final String message;
    private final String lineText;

    public ErrorLine(int line, Scene scene, String message, String lineText, boolean isWarn) {
        this.line = line;
        this.scene = scene;
        if(scene != null) {
            this.fileName = scene.getSnakeCase() + NarrativeCraftFile.EXTENSION_SCRIPT_FILE;
        }
        this.message = message;
        this.lineText = lineText;
        this.isWarn = isWarn;
    }

    public Component toMessage() {
        if(scene == null) {
            return Component.empty()
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
        } else {
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
                            .withColor(isWarn ? ChatFormatting.YELLOW.getColor() : ChatFormatting.RED.getColor())
                            .withStyle(style -> style.withBold(false))
                    )
                    .append("\n");
        }
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

    public boolean isWarn() {
        return isWarn;
    }
}