package fr.loudo.narrativecraft.screens.cameraAngles;

import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleController;
import fr.loudo.narrativecraft.screens.animations.AnimationCharacterLinkScreen;
import fr.loudo.narrativecraft.screens.components.AddCharacterListScreen;
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
        int spacing = 5;
        int totalWidth = BUTTON_WIDTH * 4 + spacing * 3;
        int startX = (this.width - totalWidth) / 2;
        int y = this.height - 50;

        Button addKeyframe = Button.builder(ImageFontConstants.ADD_KEYFRAME, button -> {
            CameraAngleInfoKeyframeScreen screen = new CameraAngleInfoKeyframeScreen(cameraAngleController);
            this.minecraft.setScreen(screen);
        }).bounds(startX, y, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(addKeyframe);

        Button addCharacter = Button.builder(ImageFontConstants.CHARACTER_ADD, button -> {
            AddCharacterListScreen addCharacterListScreen = new AddCharacterListScreen(cameraAngleController.getCameraAngleGroup());
            minecraft.setScreen(addCharacterListScreen);
        }).bounds(startX + (BUTTON_WIDTH + spacing) * 1, y, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(addCharacter);

        Button recordMenu = Button.builder(ImageFontConstants.SETTINGS, button -> {
            CameraAngleAddRecord cameraAngleAddRecord = new CameraAngleAddRecord(cameraAngleController.getCameraAngleGroup());
            minecraft.setScreen(cameraAngleAddRecord);
        }).bounds(startX + (BUTTON_WIDTH + spacing) * 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(recordMenu);

        Button saveButton = Button.builder(ImageFontConstants.SAVE, button -> {
            cameraAngleController.stopSession();
            this.onClose();
        }).bounds(startX + (BUTTON_WIDTH + spacing) * 3, y, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(saveButton);

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
