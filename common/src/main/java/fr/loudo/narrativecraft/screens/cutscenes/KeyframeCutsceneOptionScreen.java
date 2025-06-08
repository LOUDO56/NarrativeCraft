package fr.loudo.narrativecraft.screens.cutscenes;

import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutscenePlayback;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeCoordinate;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeGroup;
import fr.loudo.narrativecraft.screens.keyframes.KeyframeAdvancedSettings;
import fr.loudo.narrativecraft.screens.keyframes.KeyframeOptionScreen;
import fr.loudo.narrativecraft.utils.MathUtils;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class KeyframeCutsceneOptionScreen extends KeyframeOptionScreen {

    private final CutsceneController cutsceneController;
    private EditBox startDelayBox, pathTimeBox, transitionDelayBox, speedBox;

    public KeyframeCutsceneOptionScreen(Keyframe keyframe, ServerPlayer player) {
        super(keyframe, player);
        this.cutsceneController = (CutsceneController) playerSession.getKeyframeControllerBase();
    }

    @Override
    protected void init() {
        updateCurrentTick();
        if(!keyframe.isParentGroup()) {
            pathTimeBox = addLabeledEditBox(Translation.message("screen.keyframe_option.path_time"), String.valueOf(MathUtils.getSecondsByMillis(keyframe.getPathTime())));
            speedBox = addLabeledEditBox(Translation.message("screen.keyframe_option.speed"), String.valueOf(keyframe.getSpeed()));
        }
        if(cutsceneController.isLastKeyframe(cutsceneController.getKeyframeGroupByKeyframe(keyframe), keyframe)) {
            if(!cutsceneController.isLastKeyframe(keyframe)) {
                transitionDelayBox = addLabeledEditBox(Translation.message("screen.keyframe_option.transition_delay"), String.valueOf(MathUtils.getSecondsByMillis(keyframe.getTransitionDelay())));
            }
        } else {
            startDelayBox = addLabeledEditBox(Translation.message("screen.keyframe_option.start_delay"), String.valueOf(MathUtils.getSecondsByMillis(keyframe.getStartDelay())));
        }
        initPositionLabelBox();
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

    protected void initButtons() {
        //currentY -= 30;
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
                CutscenePlayback cutscenePlayback = new CutscenePlayback(player, cutsceneController.getCutscene().getKeyframeGroupList(), keyframe, cutsceneController);
                cutscenePlayback.start();
                playerSession.setCutscenePlayback(cutscenePlayback);
                this.onClose();
            }
        }).bounds(updateButton.getWidth() + updateButton.getX() + 5, currentY, this.font.width(playTitle) + margin, BUTTON_HEIGHT).build();
        currentY += BUTTON_HEIGHT + gap - 10;
        Component advancedTitle = Translation.message("screen.keyframe_option.advanced");
        Button advancedButton = Button.builder(advancedTitle, button -> {
            KeyframeAdvancedSettings screen = new KeyframeAdvancedSettings(cutsceneController, this, keyframe);
            this.minecraft.setScreen(screen);
        }).bounds(INITIAL_POS_X, currentY, this.font.width(advancedTitle) + margin, BUTTON_HEIGHT).build();
        currentY += BUTTON_HEIGHT + gap;
        Component removeTitle = Translation.message("screen.keyframe_option.remove");
        Button removeKeyframe = Button.builder(removeTitle, button -> {
            if(playerSession != null) {
                cutsceneController.clearCurrentPreviewKeyframe();
                cutsceneController.removeKeyframe(keyframe);
                updateCurrentTick();
                this.onClose();
            }
        }).bounds(INITIAL_POS_X, currentY, this.font.width(removeTitle) + margin, BUTTON_HEIGHT).build();
        this.addRenderableWidget(updateButton);
        this.addRenderableWidget(advancedButton);
        this.addRenderableWidget(playFromHere);
        this.addRenderableWidget(removeKeyframe);
    }

    protected void initTextSelectedKeyframe() {
        int y = 10;

        KeyframeGroup group = cutsceneController.getKeyframeGroupByKeyframe(keyframe);
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

    protected void initLittleButtons() {
        int currentX = this.width - INITIAL_POS_X;
        int gap = 5;
        int width = 20;
        Button closeButton = Button.builder(Component.literal("✖"), button -> {
            cutsceneController.clearCurrentPreviewKeyframe();
            this.onClose();
        }).bounds(currentX - (width / 2), INITIAL_POS_Y - 5, width, BUTTON_HEIGHT).build();
        Keyframe nextKeyframe = cutsceneController.getNextKeyframe(keyframe);
        if(nextKeyframe != null) {
            currentX -= INITIAL_POS_X + gap;
            Button rightKeyframeButton = Button.builder(Component.literal("▶"), button -> {
                cutsceneController.setCurrentPreviewKeyframe(nextKeyframe, false);
                cutsceneController.changeTimePosition(nextKeyframe.getTick(), false);
            }).bounds(currentX - (width / 2), INITIAL_POS_Y - 5, width, BUTTON_HEIGHT).build();
            this.addRenderableWidget(rightKeyframeButton);
        }
        Keyframe previousKeyframe = cutsceneController.getPreviousKeyframe(keyframe);
        if(previousKeyframe != null) {
            currentX -= INITIAL_POS_X + gap;
            Button leftKeyframeButton = Button.builder(Component.literal("◀"), button -> {
                cutsceneController.setCurrentPreviewKeyframe(previousKeyframe, false);
                cutsceneController.changeTimePosition(previousKeyframe.getTick(), false);
            }).bounds(currentX - (width / 2), INITIAL_POS_Y - 5, width, BUTTON_HEIGHT).build();
            this.addRenderableWidget(leftKeyframeButton);
        }
        this.addRenderableWidget(closeButton);
    }

    protected void updateValues() {
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
        if(!keyframe.isFixed()) {
            float pathTimeVal = pathTimeBox == null ? 0 : Float.parseFloat((pathTimeBox.getValue()));
            keyframe.setPathTime(MathUtils.getMillisBySecond(pathTimeVal));
        }
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

    protected void updateCurrentTick() {
        List<KeyframeGroup> keyframeGroupList = cutsceneController.getCutscene().getKeyframeGroupList();

        int initialFirstTick = 0;
        if (!keyframeGroupList.isEmpty() && !keyframeGroupList.getFirst().getKeyframeList().isEmpty()) {
            initialFirstTick = keyframeGroupList.getFirst().getKeyframeList().getFirst().getTick();
        }

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

                int newTick;
                if (i == 0 && j == 0) {
                    newTick = initialFirstTick;
                } else if (j > 0) {
                    Keyframe previous = keyframes.get(j - 1);
                    double startDelay = previous.getStartDelay() / 1000.0;
                    double pathTime = current.getPathTime() / 1000.0;
                    newTick = previous.getTick() + (int) ((startDelay + pathTime) * 20);
                } else {
                    newTick = referenceTick;
                }

                current.setTick(newTick);
                referenceTick = newTick;
            }
        }
        cutsceneController.changeTimePosition(keyframe.getTick(), true);
    }
}
