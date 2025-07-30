package fr.loudo.narrativecraft.narrative.story.inkAction;

import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.dialog.Dialog;
import fr.loudo.narrativecraft.narrative.dialog.Dialog2d;
import fr.loudo.narrativecraft.narrative.dialog.DialogData;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.inkAction.enums.InkTagType;
import fr.loudo.narrativecraft.narrative.story.inkAction.validation.ErrorLine;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec2;

import java.util.Arrays;
import java.util.List;

public class DialogValuesInkAction extends InkAction {

    private String value;

    public DialogValuesInkAction(StoryHandler storyHandler, String command) {
        super(storyHandler, InkTagType.DIALOG_VALUES, command);
    }

    @Override
    public InkActionResult execute() {
        if(command.length == 1) return InkActionResult.error(this.getClass(),  Translation.message("validation.missing_values").getString());
        name = command[1];
        DialogData dialogData = storyHandler.getGlobalDialogValue();
        switch (name) {
            case "offset" -> {
                Vec2 currentOffset = dialogData.getOffset();
                float offsetX;
                float offsetY = currentOffset.y;
                try {
                    offsetX = Float.parseFloat(command[2]);
                } catch (NumberFormatException e) {
                    return InkActionResult.error(this.getClass(), Translation.message("validation.number", command[2]).getString());
                }
                if(command.length > 3) {
                    try {
                        offsetY = Float.parseFloat(command[3]);
                    } catch (NumberFormatException e) {
                        return InkActionResult.error(this.getClass(), Translation.message("validation.number", command[3]).getString());
                    }
                }
                value = offsetX + " " + offsetY;
                dialogData.setOffset(new Vec2(offsetX, offsetY));
                storyHandler.getGlobalDialogValue().setOffset(new Vec2(offsetX, offsetY));
            }
            case "scale" -> {
                try {
                    float scale = Float.parseFloat(command[2]);
                    value = String.valueOf(scale);
                    dialogData.setScale(scale);
                    storyHandler.getGlobalDialogValue().setScale(scale);
                } catch (NumberFormatException e) {
                    return InkActionResult.error(this.getClass(), Translation.message("validation.number", command[2]).getString());
                }
            }
            case "padding" -> {
                float paddingX;
                float paddingY = dialogData.getPaddingY();
                try {
                    paddingX = Float.parseFloat(command[2]);
                } catch (NumberFormatException e) {
                    return InkActionResult.error(this.getClass(), Translation.message("validation.number", command[2]).getString());
                }
                if(command.length > 3) {
                    try {
                        paddingY = Float.parseFloat(command[3]);
                    } catch (NumberFormatException e) {
                        return InkActionResult.error(this.getClass(), Translation.message("validation.number", command[3]).getString());
                    }
                }
                value = paddingX + " " + paddingY;
                dialogData.setPaddingX(paddingX);
                dialogData.setPaddingY(paddingY);
                storyHandler.getGlobalDialogValue().setPaddingX(paddingX);
                storyHandler.getGlobalDialogValue().setPaddingY(paddingY);
            }
            case "width" -> {
                try {
                    int width = Integer.parseInt(command[2]);
                    dialogData.setMaxWidth(width);
                    dialogData.setText(storyHandler.getCurrentDialog());
                    value = String.valueOf(width);
                    storyHandler.getGlobalDialogValue().setMaxWidth(width);
                } catch (NumberFormatException e) {
                    return InkActionResult.error(this.getClass(), Translation.message("validation.number", command[2]).getString());
                }
            }
            case "textColor" -> {
                try {
                    int textColor = Integer.parseInt(command[2], 16);
                    dialogData.setTextColor(textColor);
                    value = String.valueOf(textColor);
                    storyHandler.getGlobalDialogValue().setTextColor(textColor);
                } catch (NumberFormatException e) {
                    return InkActionResult.error(this.getClass(), Translation.message("validation.number", command[2]).getString());
                }
            }
            case "backgroundColor" -> {
                try {
                    int bcColor = Integer.parseInt(command[2], 16);
                    dialogData.setBackgroundColor(bcColor);
                    value = String.valueOf(bcColor);
                    storyHandler.getGlobalDialogValue().setBackgroundColor((bcColor));
                } catch (NumberFormatException e) {
                    return InkActionResult.error(this.getClass(), Translation.message("validation.number", command[2]).getString());
                }
            }
            case "gap" -> {
                try {
                    float gap = Integer.parseInt(command[2]);
                    dialogData.setGap(gap);
                    value = String.valueOf(gap);
                    storyHandler.getGlobalDialogValue().setGap(gap);
                } catch (NumberFormatException e) {
                    return InkActionResult.error(this.getClass(), Translation.message("validation.number", command[2]).getString());
                }
            }
            case "letterSpacing" -> {
                try {
                    float letterSpacing = Integer.parseInt(command[2]);
                    dialogData.setLetterSpacing(letterSpacing);
                    value = String.valueOf(letterSpacing);
                    storyHandler.getGlobalDialogValue().setLetterSpacing(letterSpacing);
                } catch (NumberFormatException e) {
                    return InkActionResult.error(this.getClass(), Translation.message("validation.number", command[2]).getString());
                }
            }
            case "unSkippable" -> {
                try {
                    boolean unSkippable = Boolean.parseBoolean(command[2]);
                    dialogData.setUnSkippable(unSkippable);
                    value = command[2];
                } catch (RuntimeException e) {
                    return InkActionResult.error(this.getClass(), Translation.message("validation.number", command[2]).getString());
                }
            }
            case "autoSkip" -> {
                try {
                    double forceTimeEnd = Double.parseDouble(command[2]);
                    dialogData.setEndForceEndTime((long) (forceTimeEnd * 1000L));
                    value = String.valueOf(forceTimeEnd);
                } catch (NumberFormatException e) {
                    return InkActionResult.error(this.getClass(), Translation.message("validation.number", command[2]).getString());
                }
            }
            case "bobbing" -> {
                if(command.length < 4) return InkActionResult.error(this.getClass(), "");
                try {
                    float noiseShakeSpeed = Float.parseFloat(command[2]);
                    float noiseShakeStrength = Float.parseFloat(command[3]);
                    dialogData.setBobbingNoiseShakeSpeed(noiseShakeSpeed);
                    dialogData.setBobbingNoiseShakeStrength(noiseShakeStrength);
                } catch (NumberFormatException e) {
                    return InkActionResult.error(this.getClass(), Translation.message("validation.number", command[2]).getString());
                }
            }
        }
        if(storyHandler.getCurrentDialogBox() instanceof Dialog dialog) {
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
        } else if(storyHandler.getCurrentDialogBox() instanceof Dialog2d dialog2d) {
            dialog2d.setPaddingX((int)dialogData.getPaddingX());
            dialog2d.setPaddingY((int)dialogData.getPaddingY());
            dialog2d.setLetterSpacing(dialogData.getLetterSpacing());
            dialog2d.setGap(dialogData.getGap());
            dialog2d.setUnSkippable(dialogData.isUnSkippable());
            dialog2d.setForcedEndTime(dialogData.getEndForceEndTime());
            dialog2d.setTextColor(dialogData.getTextColor());
            dialog2d.setBackgroundColor(dialogData.getBackgroundColor());
        }
        sendDebugDetails();
        return InkActionResult.pass();
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
        if(command.length == 1) return new ErrorLine(line, scene, Translation.message("validation.missing_values").getString(), lineText, false);
        List<String> parameters = List.of("offset", "scale", "padding", "width", "textColor", "backgroundColor", "gap", "letterSpacing", "unSkippable", "autoSkip", "bobbing");
        if(!parameters.contains(command[1])) {
            return new ErrorLine(
                    line,
                    scene,
                    Translation.message("validation.dialog", command[1], parameters.toString()).getString(),
                    lineText, false
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
                                lineText,
                                false
                        );
                    }
                }
            }
        }
        return null;
    }
}
