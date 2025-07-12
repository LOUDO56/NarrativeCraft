package fr.loudo.narrativecraft.screens.mainScreen;

import fr.loudo.narrativecraft.narrative.story.MainScreenController;
import fr.loudo.narrativecraft.screens.components.AddCharacterListScreen;
import fr.loudo.narrativecraft.screens.keyframes.KeyframeTriggerScreen;
import fr.loudo.narrativecraft.utils.ImageFontConstants;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MainScreenControllerScreen extends Screen {

    private final int BUTTON_HEIGHT = 20;
    private final int BUTTON_WIDTH = 30;
    private final MainScreenController mainScreenController;

    public MainScreenControllerScreen(MainScreenController mainScreenController) {
        super(Component.literal("Main Screen Controller Screen"));
        this.mainScreenController = mainScreenController;
    }

    @Override
    protected void init() {
        int spacing = 5;
        int totalWidth = BUTTON_WIDTH * 4 + spacing * 4;
        int startX = (this.width - totalWidth) / 2;
        int y = this.height - 50;

        Button addKeyframe = Button.builder(ImageFontConstants.ADD_KEYFRAME, button -> {
            mainScreenController.addKeyframe("main");
        }).bounds(startX, y, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        addKeyframe.setTooltip(Tooltip.create(Translation.message("screen.camera_angle_controller.tooltip.keyframe_group")));
        this.addRenderableWidget(addKeyframe);

        Button addCharacter = Button.builder(ImageFontConstants.CHARACTER_ADD, button -> {
            AddCharacterListScreen addCharacterListScreen = new AddCharacterListScreen(mainScreenController.getCameraAngleGroup());
            minecraft.setScreen(addCharacterListScreen);
        }).bounds(startX + (BUTTON_WIDTH + spacing), y, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        addCharacter.setTooltip(Tooltip.create(Translation.message("screen.camera_angle_controller.tooltip.character")));
        this.addRenderableWidget(addCharacter);

        Button addTriggerKeyframeButton = Button.builder(ImageFontConstants.ADD_KEYFRAME_TRIGGER, button -> {
            KeyframeTriggerScreen screen = new KeyframeTriggerScreen(mainScreenController, 0);
            minecraft.setScreen(screen);
        }).bounds(startX + (BUTTON_WIDTH + spacing) * 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        addTriggerKeyframeButton.setTooltip(Tooltip.create(Translation.message("screen.cutscene_controller.tooltip.keyframe_trigger")));
        this.addRenderableWidget(addTriggerKeyframeButton);

        Button saveButton = Button.builder(ImageFontConstants.SAVE, button -> {
            mainScreenController.stopSession(true);
            this.onClose();
        }).bounds(startX + (BUTTON_WIDTH + spacing) * 3, y, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(saveButton);

        Button closeButton = Button.builder(Component.literal("✖"), button -> {
            mainScreenController.stopSession(false);
            this.onClose();
        }).bounds(startX + (BUTTON_WIDTH + spacing) * 4, y, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(closeButton);

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
