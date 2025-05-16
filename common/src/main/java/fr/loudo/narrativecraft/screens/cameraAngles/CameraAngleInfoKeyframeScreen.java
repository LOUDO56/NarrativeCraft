package fr.loudo.narrativecraft.screens.cameraAngles;

import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngle;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleController;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CameraAngleInfoKeyframeScreen extends Screen {

    private static final int ELEMENT_WIDTH = 200;
    private static final int GAP = 5;

    private CameraAngleController cameraAngleController;
    private CameraAngle cameraAngle;
    private StringWidget nameWidget;
    private EditBox editBox;
    private Button addButton;

    public CameraAngleInfoKeyframeScreen(CameraAngleController cameraAngleController) {
        super(Component.literal("Camera Angle Keyframe Info"));
        this.cameraAngleController = cameraAngleController;
    }

    public CameraAngleInfoKeyframeScreen(CameraAngleController cameraAngleController, CameraAngle cameraAngle) {
        super(Component.literal("Camera Angle Keyframe Info"));
        this.cameraAngleController = cameraAngleController;
        this.cameraAngle = cameraAngle;
    }


    @Override
    protected void init() {
        int centerX = this.width / 2;
        int totalHeight = 3 * 20 + 2 * GAP;
        int startY = (this.height - totalHeight) / 2;

        nameWidget = new StringWidget(centerX - ELEMENT_WIDTH / 2, startY, ELEMENT_WIDTH, 20, Component.literal("name"), this.font);
        this.addRenderableWidget(nameWidget);

        editBox = new EditBox(this.font, centerX - ELEMENT_WIDTH / 2, startY + 20 + GAP, ELEMENT_WIDTH, 20, cameraAngle == null ? Component.literal("") : Component.literal(cameraAngle.getName()));
        this.addRenderableWidget(editBox);

        addButton = Button.builder(cameraAngle == null ? Component.literal("add") : Component.literal("edit"), button -> {
            if(cameraAngle == null) {
                cameraAngleController.addKeyframe(editBox.getValue());
            } else {
                cameraAngleController.editKeyframe(cameraAngle, editBox.getValue());
            }
            this.onClose();
        }).bounds(centerX - ELEMENT_WIDTH / 2, startY + 2 * (20 + GAP), ELEMENT_WIDTH, 20).build();
        this.addRenderableWidget(addButton);
    }

}
