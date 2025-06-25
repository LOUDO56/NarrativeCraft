package fr.loudo.narrativecraft.screens.cameraAngles;

import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngle;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleController;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CameraAngleInfoKeyframeScreen extends Screen {

    private static final int ELEMENT_WIDTH = 200;
    private static final int GAP = 5;

    private final CameraAngleController cameraAngleController;
    private CameraAngle cameraAngle;
    private EditBox editBox;

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
    public void onClose() {
        PlayerSession playerSession = Utils.getSessionOrNull(Minecraft.getInstance().player.getUUID());
        if(playerSession != null && cameraAngle != null) {
            CameraAngleOptionsScreen screen = new CameraAngleOptionsScreen(cameraAngle, playerSession.getPlayer(), false);
            minecraft.setScreen(screen);
        } else {
            super.onClose();
        }
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int totalHeight = 3 * 20 + 2 * GAP;
        int startY = (this.height - totalHeight) / 2;

        StringWidget nameWidget = new StringWidget(centerX - ELEMENT_WIDTH / 2, startY, ELEMENT_WIDTH, 20, cameraAngle == null ? Translation.message("screen.camera_angle.add") : Translation.message("screen.camera_angle.edit", cameraAngle.getName()), this.font);
        this.addRenderableWidget(nameWidget);

        editBox = new EditBox(this.font, centerX - ELEMENT_WIDTH / 2, startY + 20 + GAP, ELEMENT_WIDTH, 20, cameraAngle == null ? Component.literal("") : Component.literal(cameraAngle.getName()));
        if(cameraAngle != null) editBox.setValue(cameraAngle.getName());
        this.addRenderableWidget(editBox);

        Button addButton = Button.builder(cameraAngle == null ? Translation.message("screen.add.text") : Translation.message("screen.update.text"), button -> {
            if (editBox.getValue().isEmpty()) {
                ScreenUtils.sendToast(Translation.message("global.error"), Translation.message("screen.story.name.required"));
                return;
            }
            if (cameraAngle == null) {
                cameraAngleController.addKeyframe(editBox.getValue());
            } else {
                cameraAngleController.editKeyframe(cameraAngle, editBox.getValue());
            }
            this.onClose();
        }).bounds(centerX - ELEMENT_WIDTH / 2, startY + 2 * (20 + GAP), ELEMENT_WIDTH, 20).build();
        this.addRenderableWidget(addButton);
    }

}
