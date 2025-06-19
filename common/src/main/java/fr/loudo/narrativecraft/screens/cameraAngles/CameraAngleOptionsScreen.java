package fr.loudo.narrativecraft.screens.cameraAngles;

import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngle;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeCoordinate;
import fr.loudo.narrativecraft.screens.keyframes.KeyframeOptionScreen;
import fr.loudo.narrativecraft.utils.ImageFontConstants;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class CameraAngleOptionsScreen extends KeyframeOptionScreen {

    private final CameraAngleController cameraAngleController;
    
    public CameraAngleOptionsScreen(Keyframe keyframe, ServerPlayer player) {
        super(keyframe, player);
        this.cameraAngleController = (CameraAngleController) playerSession.getKeyframeControllerBase();
    }

    @Override
    protected void init() {
        initPositionLabelBox();
        initButtons();
        initSliders();
        initTextSelectedKeyframe();
        initLittleButtons();
        currentY = INITIAL_POS_Y;
    }

    @Override
    protected void initLittleButtons() {
        int currentX = this.width - INITIAL_POS_X;
        int gap = 5;
        int width = 20;
        Button closeButton = Button.builder(Component.literal("✖"), button -> {
            cameraAngleController.clearCurrentPreviewKeyframe();
            this.onClose();
        }).bounds(currentX - (width / 2), INITIAL_POS_Y - 5, width, BUTTON_HEIGHT).build();
        Keyframe nextKeyframe = cameraAngleController.getNextKeyframe(keyframe);
        if(nextKeyframe != null) {
            currentX -= INITIAL_POS_X + gap;
            Button rightKeyframeButton = Button.builder(Component.literal("▶"), button -> {
                cameraAngleController.setCurrentPreviewKeyframe(nextKeyframe);
            }).bounds(currentX - (width / 2), INITIAL_POS_Y - 5, width, BUTTON_HEIGHT).build();
            this.addRenderableWidget(rightKeyframeButton);
        }
        Keyframe previousKeyframe = cameraAngleController.getPreviousKeyframe(keyframe);
        if(previousKeyframe != null) {
            currentX -= INITIAL_POS_X + gap;
            Button leftKeyframeButton = Button.builder(Component.literal("◀"), button -> {
                cameraAngleController.setCurrentPreviewKeyframe(previousKeyframe);
            }).bounds(currentX - (width / 2), INITIAL_POS_Y - 5, width, BUTTON_HEIGHT).build();
            this.addRenderableWidget(leftKeyframeButton);
        }
        this.addRenderableWidget(closeButton);
        currentX -= INITIAL_POS_X + gap;
        Button editButton = Button.builder(ImageFontConstants.EDIT, button -> {
            CameraAngleInfoKeyframeScreen screen = new CameraAngleInfoKeyframeScreen(cameraAngleController, (CameraAngle) keyframe);
            minecraft.setScreen(screen);
        }).bounds(currentX - (width / 2), INITIAL_POS_Y - 5, width, BUTTON_HEIGHT).build();
        this.addRenderableWidget(editButton);
    }

    @Override
    protected void initTextSelectedKeyframe() {
        int y = 10;
        String cameraAngleName = ((CameraAngle)keyframe).getName();
        this.addRenderableWidget(ScreenUtils.text(Component.literal(cameraAngleName), this.font, this.width / 2 - this.font.width(cameraAngleName) / 2, y));
    }

    @Override
    protected void initButtons() {
        Component updateTitle = Translation.message("screen.keyframe_option.update");
        Button updateButton = Button.builder(updateTitle, button -> {
            updateValues();
        }).bounds(INITIAL_POS_X, currentY, this.font.width(updateTitle) + 30, BUTTON_HEIGHT).build();
        this.addRenderableWidget(updateButton);
        currentY += BUTTON_HEIGHT + 15;
        Component removeTitle = Translation.message("global.remove");
        Button removeKeyframe = Button.builder(removeTitle, button -> {
            if(playerSession != null) {
                cameraAngleController.clearCurrentPreviewKeyframe();
                cameraAngleController.removeKeyframe(keyframe);
                this.onClose();
            }
        }).bounds(INITIAL_POS_X, currentY, this.font.width(removeTitle) + 15, BUTTON_HEIGHT).build();
        this.addRenderableWidget(removeKeyframe);
    }

    protected void updateValues() {
        float xVal = Float.parseFloat((coordinatesBoxList.get(0).getValue()));
        float yVal = Float.parseFloat((coordinatesBoxList.get(1).getValue()));
        float zVal = Float.parseFloat((coordinatesBoxList.get(2).getValue()));
        KeyframeCoordinate position = keyframe.getKeyframeCoordinate();
        position.setXRot(upDownValue);
        position.setYRot(leftRightValue);
        position.setZRot(rotationValue);
        position.setX(xVal);
        position.setY(yVal);
        position.setZ(zVal);
        position.setFov(fovValue);
        keyframe.setKeyframeCoordinate(position);
        keyframe.updateEntityData(player);
    }

}
