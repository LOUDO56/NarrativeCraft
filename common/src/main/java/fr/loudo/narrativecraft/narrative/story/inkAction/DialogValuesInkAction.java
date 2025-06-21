package fr.loudo.narrativecraft.narrative.story.inkAction;

import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.dialog.Dialog;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec2;

import java.util.Arrays;
import java.util.List;

public class DialogValuesInkAction extends InkAction {

    private String value;

    public DialogValuesInkAction(StoryHandler storyHandler) {
        super(storyHandler);
    }

    public DialogValuesInkAction() {}

    @Override
    public InkActionResult execute(String[] command) {
        if(command.length == 1) return InkActionResult.ERROR;
        name = command[1];
        Dialog dialog = storyHandler.getCurrentDialogBox();
        if(dialog == null) {
            // Initializing dialog if not set to apply new values
            storyHandler.showDialog();
        }
        dialog = storyHandler.getCurrentDialogBox();
        // But if it's still null, then something is wrong.
        if(dialog == null) return InkActionResult.ERROR;
        switch (name) {
            case "offset" -> {
                Vec2 currentOffset = dialog.getDialogOffset();
                float offsetX;
                float offsetY = currentOffset.y;
                try {
                    offsetX = Float.parseFloat(command[2]);
                    if(command.length > 3) {
                        offsetY = Float.parseFloat(command[3]);
                    }
                    value = offsetX + " " + offsetY;
                    dialog.setDialogOffset(new Vec2(offsetX, offsetY));
                } catch (RuntimeException e) {
                    return InkActionResult.ERROR;
                }
            }
            case "scale" -> {
                try {
                    float scale = Float.parseFloat(command[2]);
                    value = String.valueOf(scale);
                    dialog.setScale(scale);
                    if(!dialog.isAcceptNewDialog()) {
                        dialog.setOldScale(scale);
                    }
                } catch (NumberFormatException e) {
                    return InkActionResult.ERROR;
                }
            }
            case "padding" -> {
                float paddingX;
                float paddingY = dialog.getPaddingY();
                try {
                    paddingX = Float.parseFloat(command[2]);
                    if(command.length > 3) {
                        paddingY = Float.parseFloat(command[3]);
                    }
                    value = paddingX + " " + paddingY;
                    dialog.setPaddingX(paddingX);
                    dialog.setPaddingY(paddingY);
                } catch (RuntimeException e) {
                    return InkActionResult.ERROR;
                }
            }
            case "width" -> {
                try {
                    int width = Integer.parseInt(command[2]);
                    dialog.setMaxWidth(width);
                    value = String.valueOf(width);
                } catch (RuntimeException e) {
                    return InkActionResult.ERROR;
                }
            }
            case "textColor" -> {
                try {
                    int textColor = Integer.parseInt(command[2], 16);
                    dialog.setTextDialogColor(textColor);
                    value = String.valueOf(textColor);
                } catch (RuntimeException e) {
                    return InkActionResult.ERROR;
                }
            }
            case "backgroundColor" -> {
                try {
                    int bcColor = Integer.parseInt(command[2], 16);
                    dialog.setDialogBackgroundColor((255 << 24) | (bcColor & 0x00FFFFFF));
                    value = String.valueOf(bcColor);
                } catch (RuntimeException e) {
                    return InkActionResult.ERROR;
                }
            }
            case "gap" -> {
                try {
                    float gap = Integer.parseInt(command[2]);
                    dialog.setGap(gap);
                    value = String.valueOf(gap);
                } catch (RuntimeException e) {
                    return InkActionResult.ERROR;
                }
            }
            case "letterSpacing" -> {
                try {
                    float letterSpacing = Integer.parseInt(command[2]);
                    dialog.setLetterSpacing(letterSpacing);
                    value = String.valueOf(letterSpacing);
                } catch (RuntimeException e) {
                    return InkActionResult.ERROR;
                }
            }
            case "unSkippable" -> {
                try {
                    dialog.setUnSkippable(true);
                    value = "true";
                } catch (RuntimeException e) {
                    return InkActionResult.ERROR;
                }
            }
        }
        sendDebugDetails();
        return InkActionResult.PASS;
    }

    @Override
    void sendDebugDetails() {
        if(storyHandler.isDebugMode()) {
            Minecraft.getInstance().player.displayClientMessage(
                    Translation.message("debug.dialog_values", name, value),
                    false
            );
        }
    }

    @Override
    public ErrorLine validate(String[] command, int line, String lineText, Scene scene) {
        if(command.length == 1) return new ErrorLine(line, scene, Translation.message("validation.missing_values").getString(), lineText);
        List<String> parameters = List.of("offset", "scale", "padding", "width", "textColor", "backgroundColor", "gap", "letterSpacing", "unSkippable");
        if(!parameters.contains(command[1])) {
            return new ErrorLine(
                    line,
                    scene,
                    Translation.message("validation.dialog", command[1], parameters.toString()).getString(),
                    lineText
            );
        }
        List<String> values = Arrays.stream(command).toList().subList(2, command.length);
        for(String value :  values) {
            try {
                Float.parseFloat(value);
            } catch (NumberFormatException e) {
                try {
                    Integer.parseInt(value, 16);
                } catch (NumberFormatException ex) {
                    return new ErrorLine(
                            line,
                            scene,
                            Translation.message("validation.number", value).getString(),
                            lineText
                    );
                }
            }
        }
        return null;
    }
}
