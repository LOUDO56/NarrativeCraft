package fr.loudo.narrativecraft.screens.cameraAngles;

import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleController;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class CameraAngleCharacterScreen extends Screen {

    private final int BUTTON_WIDTH = 50;
    private final int BUTTON_HEIGHT = 20;
    private final CameraAngleController cameraAngleController;
    private final Entity entity;

    public CameraAngleCharacterScreen(Entity entity, CameraAngleController cameraAngleController) {
        super(Component.literal("Character screen"));
        this.cameraAngleController = cameraAngleController;
        this.entity = entity;
    }

    @Override
    protected void init() {
        int totalWidth = BUTTON_WIDTH * 2 + 5;
        int startX = (this.width - totalWidth) / 2;

        Button removeButton = Button.builder(Component.literal("Remove"), button -> {
            cameraAngleController.removeCharacter(entity);
            this.onClose();
        }).bounds(startX, this.height / 2, BUTTON_WIDTH, BUTTON_HEIGHT).build();

        Button closeButton = Button.builder(Component.literal("Close"), button -> {
            this.onClose();
        }).bounds(startX + BUTTON_WIDTH + 5, this.height / 2, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(removeButton);
        this.addRenderableWidget(closeButton);
    }


}
