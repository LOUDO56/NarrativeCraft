package fr.loudo.narrativecraft.screens;

import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutscenePlayback;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeCoordinate;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeGroup;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.utils.MathUtils;
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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Locale;

public class KeyframeOptionScreen extends Screen {

    private final int INITIAL_POS_X = 20;
    private final int INITIAL_POS_Y = 15;
    private final int EDIT_BOX_WIDTH = 60;
    private final int EDIT_BOX_HEIGHT = 15;
    private final int BUTTON_HEIGHT = 20;

    private ServerPlayer player;
    private PlayerSession playerSession;
    private Keyframe keyframe;

    private EditBox startDelayBox, pathTimeBox, transitionDelayBox, speedBox;

    private float upDownValue, leftRightValue, rotationValue, fovValue;
    private int currentY = INITIAL_POS_Y;


    public KeyframeOptionScreen(Keyframe keyframe, ServerPlayer player) {
        super(Component.literal("Keyframe Option"));
        this.keyframe = keyframe;
        this.player = player;
        this.upDownValue = keyframe.getKeyframeCoordinate().getXRot();
        this.leftRightValue = keyframe.getKeyframeCoordinate().getYRot();
        this.rotationValue = keyframe.getKeyframeCoordinate().getZRot();
        this.fovValue = keyframe.getKeyframeCoordinate().getFov();
        this.playerSession = Utils.getSessionOrNull(player);
    }

    @Override
    protected void init() {
        CutsceneController cutsceneController = playerSession.getCutsceneController();
        if(!keyframe.isParentGroup()) {
            pathTimeBox = addLabeledEditBox(Translation.message("screen.keyframe_option.path_time"), String.valueOf(MathUtils.getSecondsByMillis(keyframe.getPathTime())));
            speedBox = addLabeledEditBox(Translation.message("screen.keyframe_option.speed"), String.valueOf(keyframe.getSpeed()));
        }
        if(cutsceneController.isLastKeyframe(cutsceneController.getKeyframeGroupByKeyframe(keyframe), keyframe)) {
            transitionDelayBox = addLabeledEditBox(Translation.message("screen.keyframe_option.transition_delay"), String.valueOf(MathUtils.getSecondsByMillis(keyframe.getTransitionDelay())));

        } else {
            startDelayBox = addLabeledEditBox(Translation.message("screen.keyframe_option.start_delay"), String.valueOf(MathUtils.getSecondsByMillis(keyframe.getStartDelay())));
        }
        initSliders();
        initButtons();
        initTextSelectedKeyframe();
        initLittleButtons();
        //Reset for responsive (changing windows size or going fullscreen)
        currentY = INITIAL_POS_Y;
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

    private void initButtons() {
        currentY -= 30;
        int gap = 15;
        int margin = 15;
        Component updateTitle = Translation.message("screen.keyframe_option.update");
        Button updateButton = Button.builder(updateTitle, button -> {
            updateValues();
            updateCurrentTick();
        }).bounds(INITIAL_POS_X, currentY, this.font.width(updateTitle) + margin, BUTTON_HEIGHT).build();
        Component playTitle = Translation.message("screen.keyframe_option.play_from_here");
        Button playFromHere = Button.builder(playTitle, button -> {
            if(playerSession != null) {
                CutsceneController cutsceneController = playerSession.getCutsceneController();
                CutscenePlayback cutscenePlayback = new CutscenePlayback(player, cutsceneController.getCutscene().getKeyframeGroupList(), keyframe);
                cutscenePlayback.start();
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
                updateCurrentTick();
                this.onClose();
            }
        }).bounds(INITIAL_POS_X, currentY, this.font.width(removeTitle) + margin, BUTTON_HEIGHT).build();
        this.addRenderableWidget(updateButton);
        if(!playerSession.getCutsceneController().isLastKeyframe(keyframe)) {
            this.addRenderableWidget(playFromHere);
        }
        this.addRenderableWidget(removeKeyframe);
    }

    private void initTextSelectedKeyframe() {
        int y = 10;

        KeyframeGroup group = playerSession.getCutsceneController().getKeyframeGroupByKeyframe(keyframe);
        MutableComponent groupText = Translation.message("screen.keyframe_option.keyframe_group", group.getId());
        MutableComponent keyframeText = Translation.message("screen.keyframe_option.keyframe_id", keyframe.getId());

        int groupWidth = this.font.width(groupText);
        int keyframeWidth = this.font.width(keyframeText);
        int spacing = 5;
        int totalWidth = groupWidth + spacing + keyframeWidth;

        int startX = (this.width - totalWidth) / 2;

        StringWidget groupLabel = ScreenUtils.text(groupText, this.font, startX, y, 0x27cf1f);
        StringWidget keyframeIdLabel = ScreenUtils.text(keyframeText, this.font, startX + groupWidth + spacing, y, 0xF1C40F);

        this.addRenderableWidget(groupLabel);
        this.addRenderableWidget(keyframeIdLabel);
    }

    private void initLittleButtons() {
        int currentX = this.width - INITIAL_POS_X;
        int gap = 5;
        int width = 20;
        Button closeButton = Button.builder(Component.literal("✖"), button -> {
            playerSession.getCutsceneController().clearCurrentPreviewKeyframe();
            this.onClose();
        }).bounds(currentX - (width / 2), INITIAL_POS_Y - 5, width, BUTTON_HEIGHT).build();
        Keyframe nextKeyframe = playerSession.getCutsceneController().getNextKeyframe(keyframe);
        if(nextKeyframe != null) {
            currentX -= INITIAL_POS_X + gap;
            Button rightKeyframeButton = Button.builder(Component.literal("▶"), button -> {
                playerSession.getCutsceneController().setCurrentPreviewKeyframe(nextKeyframe, false);
                playerSession.getCutsceneController().changeTimePosition(nextKeyframe.getTick(), false);
            }).bounds(currentX - (width / 2), INITIAL_POS_Y - 5, width, BUTTON_HEIGHT).build();
            this.addRenderableWidget(rightKeyframeButton);

        }
        Keyframe previousKeyframe = playerSession.getCutsceneController().getPreviousKeyframe(keyframe);
        if(previousKeyframe != null) {
            currentX -= INITIAL_POS_X + gap;
            Button leftKeyframeButton = Button.builder(Component.literal("◀"), button -> {
                playerSession.getCutsceneController().setCurrentPreviewKeyframe(previousKeyframe, false);
                playerSession.getCutsceneController().changeTimePosition(previousKeyframe.getTick(), false);
            }).bounds(currentX - (width / 2), INITIAL_POS_Y - 5, width, BUTTON_HEIGHT).build();
            this.addRenderableWidget(leftKeyframeButton);
        }
        this.addRenderableWidget(closeButton);
    }

    private void initSliders() {
        float defaultValXRot = keyframe.getKeyframeCoordinate().getXRot() + 90F;
        Component upDownName = Translation.message("screen.keyframe_option.up_down", String.format(Locale.US, "%.2f", defaultValXRot));
        int initialY = this.height - 50;
        int gap = 5;
        int numSliders = 4;
        int sliderWidth = (this.width - gap * (numSliders + 1)) / numSliders;
        int currentX = gap;

        AbstractSliderButton upDownSlider = new AbstractSliderButton(currentX, initialY, sliderWidth, BUTTON_HEIGHT,
                upDownName,
                defaultValXRot / 180F
        ) {
            @Override
            protected void updateMessage() {
                this.setMessage(Translation.message(
                        "screen.keyframe_option.up_down",
                        String.format(Locale.US, "%.2f", getValue() + 90F)
                ));
            }

            @Override
            protected void applyValue() {
                upDownValue = getValue();
                updateValues();
            }

            public float getValue() {
                return (float) (this.value * 180F - 90F);
            }
        };

        EditBox upDownBox = new EditBox(this.font, upDownSlider.getX(), upDownSlider.getY() + BUTTON_HEIGHT + 5, EDIT_BOX_WIDTH, EDIT_BOX_HEIGHT, Component.literal("Up Down Value"));
        upDownBox.setValue(String.format(Locale.US, "%.2f", defaultValXRot));
        Button upDownButton = Button.builder(Component.literal("✔"), button -> {
            upDownValue = Float.parseFloat(upDownBox.getValue()) - 90F;
            updateValues();
        }).bounds(upDownBox.getX() + upDownBox.getWidth() + 5, upDownBox.getY(), 20, EDIT_BOX_HEIGHT).build();

        currentX += sliderWidth + gap;

        float defaultValYRot = MathUtils.get360Angle(keyframe.getKeyframeCoordinate().getYRot());
        Component leftRightName = Translation.message("screen.keyframe_option.left_right", String.format(Locale.US, "%.2f", defaultValYRot));

        AbstractSliderButton leftRightSlider = new AbstractSliderButton(currentX, initialY, sliderWidth, BUTTON_HEIGHT, leftRightName, MathUtils.get360Angle(keyframe.getKeyframeCoordinate().getYRot()) / 360F) {
            @Override
            protected void updateMessage() {
                float value = MathUtils.get360Angle(keyframe.getKeyframeCoordinate().getYRot());
                if (value == 0F && this.value == 1F) {
                    value = 360F;
                }
                this.setMessage(Translation.message(
                        "screen.keyframe_option.left_right",
                        String.format(Locale.US, "%.2f", value)
                ));
            }

            @Override
            protected void applyValue() {
                leftRightValue = getValue();
                updateValues();
            }

            public float getValue() {
                return MathUtils.get180Angle((float) (this.value * 360));
            }
        };

        currentX += sliderWidth + gap;

        float defaultValZRot = MathUtils.get180Angle(keyframe.getKeyframeCoordinate().getZRot());
        Component rotationName = Translation.message("screen.keyframe_option.rotation", defaultValZRot);

        AbstractSliderButton rotationSlider = new AbstractSliderButton(currentX, initialY, sliderWidth, BUTTON_HEIGHT, rotationName, ((keyframe.getKeyframeCoordinate().getZRot() + 180F) % 360F) / 360F) {
            @Override
            protected void updateMessage() {
                float displayedAngle = (float) (this.value * 360 - 180);
                this.setMessage(Translation.message(
                        "screen.keyframe_option.rotation",
                        String.format(Locale.US, "%.2f", displayedAngle)
                ));
            }

            @Override
            protected void applyValue() {
                float displayedAngle = (float) (this.value * 360 - 180);
                rotationValue = (displayedAngle + 360) % 360;
                updateValues();
            }

            public float getValue() {
                return (float) ((this.value * 360 - 180 + 360) % 360);
            }
        };

        currentX += sliderWidth + gap;

        Component fovName = Translation.message("screen.keyframe_option.fov", String.format(Locale.US, "%.2f", keyframe.getKeyframeCoordinate().getFov()));

        AbstractSliderButton fovSlider = new AbstractSliderButton(currentX, initialY, sliderWidth, BUTTON_HEIGHT, fovName, (double) keyframe.getKeyframeCoordinate().getFov() / 150) {
            @Override
            protected void updateMessage() {
                this.setMessage(Translation.message("screen.keyframe_option.fov", String.format(Locale.US, "%.2f", keyframe.getKeyframeCoordinate().getFov())));
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
        this.addRenderableWidget(upDownBox);
        this.addRenderableWidget(upDownButton);
        this.addRenderableWidget(leftRightSlider);
        this.addRenderableWidget(rotationSlider);
        this.addRenderableWidget(fovSlider);

        currentY += 30;

    }

    private void updateValues() {
        if(startDelayBox != null) {
            float startDelayVal = Float.parseFloat((startDelayBox.getValue()));
            keyframe.setStartDelay(MathUtils.getMillisBySecond(startDelayVal));
        }
        if(transitionDelayBox != null) {
            float transitionDelayValue = Float.parseFloat((transitionDelayBox.getValue()));
            keyframe.setTransitionDelay(MathUtils.getMillisBySecond(transitionDelayValue));
        }
        if(speedBox != null) {
            double speedValue = Double.parseDouble((speedBox.getValue()));
            keyframe.setSpeed(speedValue);
        }
        float pathTimeVal = pathTimeBox == null ? 0 : Float.parseFloat((pathTimeBox.getValue()));
        KeyframeCoordinate position = keyframe.getKeyframeCoordinate();
        keyframe.setPathTime(MathUtils.getMillisBySecond(pathTimeVal));
        position.setXRot(upDownValue);
        position.setYRot(leftRightValue);
        position.setZRot(rotationValue);
        position.setFov(fovValue);
        keyframe.setKeyframeCoordinate(position);
        keyframe.updateEntityData(player);
    }

    private void updateCurrentTick() {
        List<KeyframeGroup> keyframeGroupList = playerSession.getCutsceneController().getCutscene().getKeyframeGroupList();

        int referenceTick = 0;

        for (int i = 0; i < keyframeGroupList.size(); i++) {
            KeyframeGroup group = keyframeGroupList.get(i);
            List<Keyframe> keyframes = group.getKeyframeList();

            if (i > 0 && !keyframeGroupList.get(i - 1).getKeyframeList().isEmpty()) {
                Keyframe lastKeyframePrevGroup = keyframeGroupList.get(i - 1).getKeyframeList().getLast();
                referenceTick += (int) (lastKeyframePrevGroup.getTransitionDelay() / 1000.0 * 20);
            }

            for (int j = 0; j < keyframes.size(); j++) {
                Keyframe current = keyframes.get(j);

                int newTick = referenceTick;
                if (j > 0) {
                    Keyframe previous = keyframes.get(j - 1);
                    double startDelay = previous.getStartDelay() / 1000.0;
                    double pathTime = current.getPathTime() / 1000.0;
                    newTick = previous.getTick() + (int) ((startDelay + pathTime) * 20);
                    if(i == keyframeGroupList.size() - 1 && j == keyframes.size() - 1) {
                        newTick += (int) ((keyframe.getTransitionDelay() / 1000) * 20);
                    }
                }

                current.setTick(newTick);
                referenceTick = newTick;
            }
        }
        playerSession.getCutsceneController().changeTimePosition(keyframe.getTick(), false);
    }
}
