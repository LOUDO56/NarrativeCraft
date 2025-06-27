package fr.loudo.narrativecraft.narrative.story.inkAction;

import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.dialog.Dialog;
import fr.loudo.narrativecraft.narrative.dialog.DialogData;
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
        DialogData dialogData = storyHandler.getGlobalDialogValue();
        switch (name) {
            case "offset" -> {
                Vec2 currentOffset = dialogData.getOffset();
                float offsetX;
                float offsetY = currentOffset.y;
                try {
                    offsetX = Float.parseFloat(command[2]);
                    if(command.length > 3) {
                        offsetY = Float.parseFloat(command[3]);
                    }
                    value = offsetX + " " + offsetY;
                    dialogData.setOffset(new Vec2(offsetX, offsetY));
                    storyHandler.getGlobalDialogValue().setOffset(new Vec2(offsetX, offsetY));
                } catch (RuntimeException e) {
                    return InkActionResult.ERROR;
                }
            }
            case "scale" -> {
                try {
                    float scale = Float.parseFloat(command[2]);
                    value = String.valueOf(scale);
                    dialogData.setScale(scale);
                    storyHandler.getGlobalDialogValue().setScale(scale);
                } catch (NumberFormatException e) {
                    return InkActionResult.ERROR;
                }
            }
            case "padding" -> {
                float paddingX;
                float paddingY = dialogData.getPaddingY();
                try {
                    paddingX = Float.parseFloat(command[2]);
                    if(command.length > 3) {
                        paddingY = Float.parseFloat(command[3]);
                    }
                    value = paddingX + " " + paddingY;
                    dialogData.setPaddingX(paddingX);
                    dialogData.setPaddingY(paddingY);
                    storyHandler.getGlobalDialogValue().setPaddingX(paddingX);
                    storyHandler.getGlobalDialogValue().setPaddingY(paddingY);
                } catch (RuntimeException e) {
                    return InkActionResult.ERROR;
                }
            }
            case "width" -> {
                try {
                    int width = Integer.parseInt(command[2]);
                    dialogData.setMaxWidth(width);
                    dialogData.setText(storyHandler.getCurrentDialog());
                    value = String.valueOf(width);
                    storyHandler.getGlobalDialogValue().setMaxWidth(width);
                } catch (RuntimeException e) {
                    return InkActionResult.ERROR;
                }
            }
            case "textColor" -> {
                try {
                    int textColor = Integer.parseInt(command[2], 16);
                    dialogData.setTextColor(textColor);
                    value = String.valueOf(textColor);
                    storyHandler.getGlobalDialogValue().setTextColor(textColor);
                } catch (RuntimeException e) {
                    return InkActionResult.ERROR;
                }
            }
            case "backgroundColor" -> {
                try {
                    int bcColor = Integer.parseInt(command[2], 16);
                    dialogData.setBackgroundColor(bcColor);
                    value = String.valueOf(bcColor);
                    storyHandler.getGlobalDialogValue().setBackgroundColor((bcColor));
                } catch (RuntimeException e) {
                    return InkActionResult.ERROR;
                }
            }
            case "gap" -> {
                try {
                    float gap = Integer.parseInt(command[2]);
                    dialogData.setGap(gap);
                    value = String.valueOf(gap);
                    storyHandler.getGlobalDialogValue().setGap(gap);
                } catch (RuntimeException e) {
                    return InkActionResult.ERROR;
                }
            }
            case "letterSpacing" -> {
                try {
                    float letterSpacing = Integer.parseInt(command[2]);
                    dialogData.setLetterSpacing(letterSpacing);
                    value = String.valueOf(letterSpacing);
                    storyHandler.getGlobalDialogValue().setLetterSpacing(letterSpacing);
                } catch (RuntimeException e) {
                    return InkActionResult.ERROR;
                }
            }
            case "unSkippable" -> {
                try {
                    boolean unSkippable = Boolean.parseBoolean(command[2]);
                    dialogData.setUnSkippable(unSkippable);
                    value = command[2];
                } catch (RuntimeException e) {
                    return InkActionResult.ERROR;
                }
            }
            case "autoSkip" -> {
                try {
                    double forceTimeEnd = Double.parseDouble(command[2]);
                    dialogData.setEndForceEndTime((long) (forceTimeEnd * 1000L));
                    value = String.valueOf(forceTimeEnd);
                } catch (RuntimeException e) {
                    return InkActionResult.ERROR;
                }
            }
            case "bobbing" -> {
                if(command.length < 4) return InkActionResult.ERROR;
                try {
                    float noiseShakeSpeed = Float.parseFloat(command[2]);
                    float noiseShakeStrength = Float.parseFloat(command[3]);
                    dialogData.setBobbingNoiseShakeSpeed(noiseShakeSpeed);
                    dialogData.setBobbingNoiseShakeStrength(noiseShakeStrength);
                } catch (NumberFormatException e) {
                    return InkActionResult.ERROR;
                }
            }
        }
        Dialog dialog = storyHandler.getCurrentDialogBox();
        if(dialog != null) {
            dialog.setPaddingX(dialogData.getPaddingX());
            dialog.setPaddingY(dialogData.getPaddingY());
            dialog.setScale(dialogData.getScale());
            dialog.setLetterSpacing(dialogData.getLetterSpacing());
            dialog.setGap(dialogData.getGap());
            dialog.setMaxWidth(dialogData.getMaxWidth());
            dialog.setDialogOffset(dialogData.getOffset());
            dialog.setUnSkippable(dialogData.isUnSkippable());
            dialog.setForcedEndTime(dialogData.getEndForceEndTime());
            dialog.setTextDialogColor(dialogData.getTextColor());
            dialog.setDialogBackgroundColor(dialogData.getBackgroundColor());
            dialog.getDialogEntityBobbing().setNoiseShakeStrength(dialogData.getBobbingNoiseShakeStrength());
            dialog.getDialogEntityBobbing().setNoiseShakeSpeed(dialogData.getBobbingNoiseShakeSpeed());
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
        List<String> parameters = List.of("offset", "scale", "padding", "width", "textColor", "backgroundColor", "gap", "letterSpacing", "unSkippable", "autoSkip", "bobbing");
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
                    try {
                        Boolean.parseBoolean(value);
                    } catch (Exception exc) {
                        return new ErrorLine(
                                line,
                                scene,
                                Translation.message("validation.number", value).getString(),
                                lineText
                        );
                    }
                }
            }
        }
        return null;
    }
}
