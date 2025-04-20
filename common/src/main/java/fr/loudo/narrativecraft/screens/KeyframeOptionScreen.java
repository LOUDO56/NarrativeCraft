package fr.loudo.narrativecraft.screens;

import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.utils.PlayerCoord;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class KeyframeOptionScreen extends Screen {

    private final int INITIAL_POS_X = 20;
    private final int INITIAL_POS_Y = 20;
    private final int EDIT_BOX_WIDTH = 60;
    private final int EDIT_BOX_HEIGHT = 15;
    private final int BUTTON_WIDTH = 60;
    private final int BUTTON_HEIGHT = 20;
    private final String REGEX_FLOAT = "[+-]?([0-9]+([.][0-9]*)?|[.][0-9]+)";

    private Keyframe keyframe;
    private int currentY = INITIAL_POS_Y;
    private List<EditBox> editBoxList;
    private AbstractSliderButton upDownSlider;
    private AbstractSliderButton leftRightSlider;
    private AbstractSliderButton rotationSlider;
    private AbstractSliderButton fovSlider;

    private float upDownValue;
    private float leftRightValue;
    private float rotationValue;
    private int fovValue;

    private ServerPlayer player;

    public KeyframeOptionScreen(Keyframe keyframe, ServerPlayer player) {
        super(Component.literal("Keyframe Option"));
        this.keyframe = keyframe;
        this.editBoxList = new ArrayList<>();
        this.player = player;
        this.upDownValue = keyframe.getPosition().getXRot();
        this.leftRightValue = keyframe.getPosition().getYRot();
        this.rotationValue = keyframe.getPosition().getZRot();
        this.fovValue = keyframe.getFov();
    }

    @Override
    protected void init() {
        addLabeledEditBox(Translation.message("screen.keyframe_option.start_delay"), String.valueOf(Utils.getSecondsByMillis(keyframe.getStartDelay())));
        addLabeledEditBox(Translation.message("screen.keyframe_option.path_time"), String.valueOf(Utils.getSecondsByMillis(keyframe.getPathTime())));
        initPositionLabelBox();
        initSliders();
        initUpdateButton();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void renderBlurredBackground() {}

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

    private void addLabeledEditBox(Component text, String defaultValue) {
        StringWidget labelWidget = ScreenUtils.text(text, this.font, INITIAL_POS_X, currentY);

        EditBox editBox = new EditBox(this.font,
                INITIAL_POS_X + labelWidget.getWidth() + 5,
                currentY - labelWidget.getHeight() / 2,
                EDIT_BOX_WIDTH,
                EDIT_BOX_HEIGHT,
                Component.literal(text.getString() + " Value"));

        editBox.setValue(defaultValue);
        editBox.setFilter((s -> s.matches(REGEX_FLOAT)));

        this.addRenderableWidget(labelWidget);
        this.addRenderableWidget(editBox);
        editBoxList.add(editBox);

        currentY += EDIT_BOX_HEIGHT + 5;
    }

    private void initPositionLabelBox() {
        int currentX = INITIAL_POS_X;
        int editWidth = 50;
        int i = 0;
        PlayerCoord position = keyframe.getPosition();
        Float[] coords = {(float)position.getX(), (float)position.getY(), (float)position.getZ(), position.getXRot(), position.getYRot()};
        String[] labels = {"X:", "Y:", "Z:"};
        for(String label : labels) {
            StringWidget stringWidget = ScreenUtils.text(Component.literal(label), this.font, currentX, currentY);
            EditBox box = new EditBox(this.font,
                    currentX + stringWidget.getWidth() + 5,
                    currentY - stringWidget.getHeight() / 2,
                    editWidth,
                    EDIT_BOX_HEIGHT,
                    Component.literal(stringWidget + " Value"));
            box.setFilter(s -> s.matches(REGEX_FLOAT));
            box.setValue(String.format("%.2f", coords[i]));
            this.addRenderableWidget(stringWidget);
            this.addRenderableWidget(box);
            editBoxList.add(box);
            currentX += stringWidget.getWidth() + editWidth + 10;
            i++;
        }
    }

    private void initUpdateButton() {
        Button updateButton = Button.builder(Translation.message("screen.keyframe_option.update"), button -> {
            updateValues();
        }).bounds(INITIAL_POS_X, currentY, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        currentY += 25;
        Button removeKeyframe = Button.builder(Translation.message("screen.keyframe_option.remove"), button -> {
            PlayerSession playerSession = Utils.getSessionOrNull(player);
            if(playerSession != null) {
                playerSession.getCutsceneController().clearCurrentPreviewKeyframe();
                playerSession.getCutsceneController().removeKeyframe(keyframe);
                this.onClose();
            }
        }).bounds(INITIAL_POS_X, currentY, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(updateButton);
        this.addRenderableWidget(removeKeyframe);
    }

    private void initSliders() {
        Component upDownName = Translation.message("screen.keyframe_option.up_down", String.format("%.2f", keyframe.getPosition().getXRot()));
        currentY += 30;
        upDownSlider = new AbstractSliderButton(INITIAL_POS_X, currentY, 150, BUTTON_HEIGHT,
                upDownName,
                (Utils.get180Angle(keyframe.getPosition().getXRot()) + 90F) / 180F
        ) {
            @Override
            protected void updateMessage() {
                this.setMessage(Translation.message("screen.keyframe_option.up_down", String.format("%.2f", keyframe.getPosition().getXRot())));
            }

            @Override
            protected void applyValue() {
                upDownValue = getValue();
                updateValues();
            }

            public float getValue() {
                return (float)(this.value * 180F - 90F);
            }
        };
        currentY += 30;

        Component leftRightName = Translation.message("screen.keyframe_option.left_right", String.format("%.2f", keyframe.getPosition().getYRot()));

        leftRightSlider = new AbstractSliderButton(INITIAL_POS_X, currentY, 150, BUTTON_HEIGHT, leftRightName, Utils.get360Angle(keyframe.getPosition().getYRot()) / 360) {
            @Override
            protected void updateMessage() {
                this.setMessage(Translation.message("screen.keyframe_option.left_right", String.format("%.2f", keyframe.getPosition().getYRot())));
            }

            @Override
            protected void applyValue() {
                leftRightValue = getValue();
                updateValues();
            }

            public float getValue() {
                return Utils.get180Angle((float) (this.value * 360));
            }
        };

        currentY += 30;

        Component rotationName = Translation.message("screen.keyframe_option.rotation", (int) keyframe.getPosition().getZRot());

        rotationSlider = new AbstractSliderButton(INITIAL_POS_X, currentY, 150, BUTTON_HEIGHT, rotationName, keyframe.getPosition().getZRot() / 360) {
            @Override
            protected void updateMessage() {
                this.setMessage(Translation.message("screen.keyframe_option.rotation", (int) keyframe.getPosition().getZRot()));
            }

            @Override
            protected void applyValue() {
                rotationValue = getValue();
                updateValues();
            }

            public int getValue() {
                return (int) (this.value * 360);
            }
        };

        currentY += 30;

        Component fovName = Translation.message("screen.keyframe_option.fov", keyframe.getFov());

        fovSlider = new AbstractSliderButton(INITIAL_POS_X, currentY, 150, BUTTON_HEIGHT, fovName,  (double) keyframe.getFov() / 150) {
            @Override
            protected void updateMessage() {
                this.setMessage(Translation.message("screen.keyframe_option.fov", keyframe.getFov()));
            }

            @Override
            protected void applyValue() {
                fovValue = getValue();
                updateValues();
            }

            public int getValue() {
                return (int) (this.value * 150);
            }
        };


        this.addRenderableWidget(upDownSlider);
        this.addRenderableWidget(leftRightSlider);
        this.addRenderableWidget(rotationSlider);
        this.addRenderableWidget(fovSlider);

        currentY += 30;

    }

    private void updateValues() {
        float startDelayVal = Float.parseFloat((editBoxList.get(0).getValue()));
        float pathTimeVal = Float.parseFloat((editBoxList.get(1).getValue()));
        float xVal = Float.parseFloat((editBoxList.get(2).getValue()));
        float yVal = Float.parseFloat((editBoxList.get(3).getValue()));
        float zVal = Float.parseFloat((editBoxList.get(4).getValue()));
        PlayerCoord position = keyframe.getPosition();
        keyframe.setStartDelay(Utils.getMillisBySecond(startDelayVal));
        keyframe.setPathTime(Utils.getMillisBySecond(pathTimeVal));
        position.setX(xVal);
        position.setY(yVal);
        position.setZ(zVal);
        position.setXRot(upDownValue);
        position.setYRot(leftRightValue);
        position.setZRot(rotationValue);
        keyframe.setPosition(position);
        keyframe.updateItemData(player);
        keyframe.setFov(fovValue);
    }
}
