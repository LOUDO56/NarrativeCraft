package fr.loudo.narrativecraft.screens.cameraAngles;

import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleController;
import fr.loudo.narrativecraft.utils.ImageFontConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CameraAngleControllerScreen extends Screen {

    private final int BUTTON_HEIGHT = 20;
    private final int BUTTON_WIDTH = 30;
    private final CameraAngleController cameraAngleController;

    public CameraAngleControllerScreen(CameraAngleController cameraAngleController) {
        super(Component.literal("Camera Angle Controller Screen"));
        this.cameraAngleController = cameraAngleController;
    }

    @Override
    protected void init() {
        Button addKeyframe = Button.builder(ImageFontConstants.ADD_KEYFRAME, button -> {
            CameraAngleInfoKeyframeScreen screen = new CameraAngleInfoKeyframeScreen(cameraAngleController);
            this.minecraft.setScreen(screen);
        }).bounds(this.width / 2 - BUTTON_WIDTH / 2, this.height - 50, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(addKeyframe);
    }

    @Override
    protected void renderBlurredBackground() {}

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
