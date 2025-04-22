package fr.loudo.narrativecraft.screens;

import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutscenePlayback;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeCoordinate;
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

    private ServerPlayer player;
    private PlayerSession playerSession;
    private Keyframe keyframe;
    private List<EditBox> coordinatesBoxList;

    private EditBox startDelayBox;
    private EditBox pathTimeBox;

    private AbstractSliderButton upDownSlider;
    private AbstractSliderButton leftRightSlider;
    private AbstractSliderButton rotationSlider;
    private AbstractSliderButton fovSlider;

    private float upDownValue;
    private float leftRightValue;
    private float rotationValue;
    private float fovValue;
    private int currentY = INITIAL_POS_Y;


    public KeyframeOptionScreen(Keyframe keyframe, ServerPlayer player) {
        super(Component.literal("Keyframe Option"));
        this.keyframe = keyframe;
        this.coordinatesBoxList = new ArrayList<>();
        this.player = player;
        this.upDownValue = keyframe.getKeyframeCoordinate().getXRot();
        this.leftRightValue = keyframe.getKeyframeCoordinate().getYRot();
        this.rotationValue = keyframe.getKeyframeCoordinate().getZRot();
        this.fovValue = keyframe.getKeyframeCoordinate().getFov();
        this.playerSession = Utils.getSessionOrNull(player);
    }

    @Override
    protected void init() {
        startDelayBox = addLabeledEditBox(Translation.message("screen.keyframe_option.start_delay"), String.valueOf(Utils.getSecondsByMillis(keyframe.getStartDelay())));
        if(!keyframe.isParentGroup()) {
            pathTimeBox = addLabeledEditBox(Translation.message("screen.keyframe_option.path_time"), String.valueOf(Utils.getSecondsByMillis(keyframe.getPathTime())));
        }
        initPositionLabelBox();
        initSliders();
        initButtons();
        initLittleButtons();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void renderBlurredBackground() {}

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

    private EditBox addLabeledEditBox(Component text, String defaultValue) {
        StringWidget labelWidget = ScreenUtils.text(text, this.font, INITIAL_POS_X, currentY);

        EditBox editBox = new EditBox(this.font,
                INITIAL_POS_X + labelWidget.getWidth() + 5,
                currentY - labelWidget.getHeight() / 2,
                EDIT_BOX_WIDTH,
                EDIT_BOX_HEIGHT,
                Component.literal(text.getString() + " Value"));

        editBox.setValue(defaultValue);
        editBox.setFilter((s -> s.matches(Utils.REGEX_FLOAT_POSITIVE_ONLY)));

        this.addRenderableWidget(labelWidget);
        this.addRenderableWidget(editBox);

        currentY += EDIT_BOX_HEIGHT + 5;
        return editBox;
    }

    private void initPositionLabelBox() {
        int currentX = INITIAL_POS_X;
        int editWidth = 50;
        int i = 0;
        KeyframeCoordinate position = keyframe.getKeyframeCoordinate();
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
            box.setFilter(s -> s.matches(Utils.REGEX_FLOAT));
            box.setValue(String.format("%.2f", coords[i]));
            this.addRenderableWidget(stringWidget);
            this.addRenderableWidget(box);
            coordinatesBoxList.add(box);
            currentX += stringWidget.getWidth() + editWidth + 10;
            i++;
        }
    }

    private void initButtons() {
        currentY -= 10;
        int gap = 15;
        int margin = 15;
        Component updateTitle = Translation.message("screen.keyframe_option.update");
        Button updateButton = Button.builder(updateTitle, button -> {
            updateValues();
        }).bounds(INITIAL_POS_X, currentY, this.font.width(updateTitle) + margin, BUTTON_HEIGHT).build();
        Component playTitle = Translation.message("screen.keyframe_option.play_from_here");
        Button playFromHere = Button.builder(playTitle, button -> {
            if(playerSession != null) {
                CutsceneController cutsceneController = playerSession.getCutsceneController();
                CutscenePlayback cutscenePlayback = new CutscenePlayback(player, cutsceneController.getCutscene().getKeyframeGroupList(), keyframe);
                cutscenePlayback.initStartFrame();
                playerSession.setCutscenePlayback(cutscenePlayback);
                this.onClose();
            }
        }).bounds(updateButton.getWidth() + updateButton.getX() + 5, currentY, this.font.width(playTitle) + margin, BUTTON_HEIGHT).build();
        currentY += BUTTON_HEIGHT + gap;
        Component removeTitle = Translation.message("screen.keyframe_option.remove");
        Button removeKeyframe = Button.builder(removeTitle, button -> {
            if(playerSession != null) {
                playerSession.getCutsceneController().clearCurrentPreviewKeyframe();
                playerSession.getCutsceneController().removeKeyframe(keyframe);
                this.onClose();
            }
        }).bounds(INITIAL_POS_X, currentY, this.font.width(removeTitle) + margin, BUTTON_HEIGHT).build();
        this.addRenderableWidget(updateButton);
        CutsceneController cutsceneController = playerSession.getCutsceneController();
        if(cutsceneController.getKeyframeGroupCounter().get() <= cutsceneController.getSelectedKeyframeGroup().getId()
        && cutsceneController.getSelectedKeyframeGroup().getKeyframeList().getLast().getId() != keyframe.getId()) {
            this.addRenderableWidget(playFromHere);
        }
        this.addRenderableWidget(removeKeyframe);
    }

    private void initLittleButtons() {
        int width = 20;
        Button closeButton = Button.builder(Component.literal("X"), button -> {
            playerSession.getCutsceneController().clearCurrentPreviewKeyframe();
            this.onClose();
        }).bounds(this.width - INITIAL_POS_X - (width / 2), startDelayBox.getY(), width, BUTTON_HEIGHT).build();
        this.addRenderableWidget(closeButton);
    }

    private void initSliders() {
        Component upDownName = Translation.message("screen.keyframe_option.up_down", String.format("%.2f", keyframe.getKeyframeCoordinate().getXRot()));
        int initialY = this.height - 40;
        int gap = 5;
        int numSliders = 4;
        int sliderWidth = (this.width - gap * (numSliders + 1)) / numSliders;
        int currentX = gap;

        upDownSlider = new AbstractSliderButton(currentX, initialY, sliderWidth, BUTTON_HEIGHT,
                upDownName,
                (Utils.get180Angle(keyframe.getKeyframeCoordinate().getXRot()) + 90F) / 180F
        ) {
            @Override
            protected void updateMessage() {
                this.setMessage(Translation.message("screen.keyframe_option.up_down", String.format("%.2f", keyframe.getKeyframeCoordinate().getXRot())));
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

        currentX += sliderWidth + gap;

        Component leftRightName = Translation.message("screen.keyframe_option.left_right", String.format("%.2f", keyframe.getKeyframeCoordinate().getYRot()));

        leftRightSlider = new AbstractSliderButton(currentX, initialY, sliderWidth, BUTTON_HEIGHT, leftRightName, Utils.get360Angle(keyframe.getKeyframeCoordinate().getYRot()) / 360) {
            @Override
            protected void updateMessage() {
                this.setMessage(Translation.message("screen.keyframe_option.left_right", String.format("%.2f", keyframe.getKeyframeCoordinate().getYRot())));
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

        currentX += sliderWidth + gap;

        Component rotationName = Translation.message("screen.keyframe_option.rotation", (int) keyframe.getKeyframeCoordinate().getZRot());

        rotationSlider = new AbstractSliderButton(currentX, initialY, sliderWidth, BUTTON_HEIGHT, rotationName, keyframe.getKeyframeCoordinate().getZRot() / 360) {
            @Override
            protected void updateMessage() {
                this.setMessage(Translation.message("screen.keyframe_option.rotation", (int) keyframe.getKeyframeCoordinate().getZRot()));
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

        currentX += sliderWidth + gap;

        Component fovName = Translation.message("screen.keyframe_option.fov", String.format("%.2f", keyframe.getKeyframeCoordinate().getFov()));

        fovSlider = new AbstractSliderButton(currentX, initialY, sliderWidth, BUTTON_HEIGHT, fovName,  (double) keyframe.getKeyframeCoordinate().getFov() / 150) {
            @Override
            protected void updateMessage() {
                this.setMessage(Translation.message("screen.keyframe_option.fov", String.format("%.2f", keyframe.getKeyframeCoordinate().getFov())));
            }

            @Override
            protected void applyValue() {
                fovValue = getValue();
                updateValues();
            }

            public float getValue() {
                return (float) (this.value * 150);
            }
        };


        this.addRenderableWidget(upDownSlider);
        this.addRenderableWidget(leftRightSlider);
        this.addRenderableWidget(rotationSlider);
        this.addRenderableWidget(fovSlider);

        currentY += 30;

    }

    private void updateValues() {
        float startDelayVal = Float.parseFloat((startDelayBox.getValue()));
        float pathTimeVal = pathTimeBox == null ? 0 : Float.parseFloat((pathTimeBox.getValue()));
//        float xVal = Float.parseFloat((coordinatesBoxList.get(0).getValue()));
//        float yVal = Float.parseFloat((coordinatesBoxList.get(1).getValue()));
//        float zVal = Float.parseFloat((coordinatesBoxList.get(2).getValue()));
        KeyframeCoordinate position = keyframe.getKeyframeCoordinate();
        keyframe.setStartDelay(Utils.getMillisBySecond(startDelayVal));
        keyframe.setPathTime(Utils.getMillisBySecond(pathTimeVal));
//        position.setX(xVal);
//        position.setY(yVal);
//        position.setZ(zVal);
        position.setXRot(upDownValue);
        position.setYRot(leftRightValue);
        position.setZRot(rotationValue);
        position.setFov(fovValue);
        keyframe.setKeyframeCoordinate(position);
        keyframe.updateItemData(player);
    }
}
