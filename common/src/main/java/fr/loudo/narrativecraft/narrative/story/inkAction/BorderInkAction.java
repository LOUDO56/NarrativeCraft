package fr.loudo.narrativecraft.narrative.story.inkAction;

import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.inkAction.enums.InkActionResult;
import fr.loudo.narrativecraft.narrative.story.inkAction.enums.InkTagType;
import fr.loudo.narrativecraft.narrative.story.inkAction.validation.ErrorLine;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.ARGB;

public class BorderInkAction extends InkAction {

    private int up, right, down, left, color;
    private double opacity;

    public BorderInkAction(StoryHandler storyHandler, String command) {
        super(storyHandler, InkTagType.BORDER, command);
    }

    @Override
    public InkActionResult execute() {
        if(command.length < 5) return InkActionResult.ERROR;
        up = 0;
        right = 0;
        down = 0;
        left = 0;
        opacity = 1;
        color = 0;
        try {
            up = Integer.parseInt(command[1]) * 3;
            right = Integer.parseInt(command[2]) * 3;
            down = Integer.parseInt(command[3]) * 3;
            left = Integer.parseInt(command[4]) * 3;
            if(command.length > 5) {
                color = Integer.parseInt(command[5], 16);
            }
            if(command.length > 6) {
                opacity = Math.clamp(Double.parseDouble(command[6]), 0, 1);
            }
        } catch (RuntimeException e) {
            return InkActionResult.ERROR;
        }
        color = ARGB.color((int)(opacity * 255), color);
        if(up == 0 && right == 0 && down == 0 && left == 0) {
            storyHandler.getInkActionList().removeIf(inkAction -> inkAction instanceof BorderInkAction);
        } else {
            storyHandler.getInkActionList().add(this);
        }
        sendDebugDetails();
        return InkActionResult.PASS;
    }

    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        int widthScreen = minecraft.getWindow().getGuiScaledWidth();
        int heightScreen = minecraft.getWindow().getGuiScaledHeight();
        int guiScale = Math.max(1, minecraft.options.guiScale().get());
        // UP
        guiGraphics.fill(0, 0, widthScreen, up / guiScale, color);

        // RIGHT
        guiGraphics.fill(widthScreen - right / guiScale, 0, widthScreen, heightScreen, color);

        // DOWN
        guiGraphics.fill(0, heightScreen - down / guiScale, widthScreen, heightScreen, color);

        // LEFT
        guiGraphics.fill(0, 0, left / guiScale, heightScreen, color);


    }

    @Override
    void sendDebugDetails() {
        if(storyHandler.isDebugMode()) {
            Minecraft.getInstance().player.displayClientMessage(
                    Translation.message("debug.border", up / 3, right / 3, down / 3, left / 3, color, opacity),
                    false
            );
        }
    }

    @Override
    public ErrorLine validate(String[] command, int line, String lineText, Scene scene) {
        if(command.length < 5) return new ErrorLine(
                line,
                scene,
                Translation.message("validation.missing_values").getString(),
                lineText,
                false
        );
        for (int i = 1; i < 4; i++) {
            try {
                Integer.parseInt(command[i]);
            } catch (NumberFormatException e) {
                return new ErrorLine(
                        line,
                        scene,
                        Translation.message("validation.number", command[i]).getString(),
                        lineText,
                        false
                );
            }
        }
        if(command.length > 5) {
            try {
                Integer.parseInt(command[5], 16);
            } catch (NumberFormatException e) {
                return new ErrorLine(
                        line,
                        scene,
                        Translation.message("validation.number", command[5]).getString(),
                        lineText,
                        false
                );
            }
        }
        if(command.length > 6) {
            try {
                Double.parseDouble(command[6]);
            } catch (NumberFormatException e) {
                return new ErrorLine(
                        line,
                        scene,
                        Translation.message("validation.number", command[6]).getString(),
                        lineText,
                        false
                );
            }
        }
        return null;
    }
}
