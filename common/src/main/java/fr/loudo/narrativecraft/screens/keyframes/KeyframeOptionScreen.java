package fr.loudo.narrativecraft.screens.keyframes;

import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeCoordinate;
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
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class KeyframeOptionScreen extends Screen {

    protected final int INITIAL_POS_X = 20;
    protected final int INITIAL_POS_Y = 15;
    protected final int EDIT_BOX_WIDTH = 60;
    protected final int EDIT_BOX_HEIGHT = 15;
    protected final int BUTTON_HEIGHT = 20;

    protected final List<EditBox> coordinatesBoxList;
    protected final ServerPlayer player;
    protected final PlayerSession playerSession;
    protected final Keyframe keyframe;

    protected float upDownValue, leftRightValue, rotationValue, fovValue;
    protected int currentY = INITIAL_POS_Y;

    protected boolean hide;

    public KeyframeOptionScreen(Keyframe keyframe, ServerPlayer player, boolean hide) {
        super(Component.literal("Keyframe Option"));
        this.keyframe = keyframe;
        this.player = player;
        this.coordinatesBoxList = new ArrayList<>();
        this.upDownValue = keyframe.getKeyframeCoordinate().getXRot();
        this.leftRightValue = keyframe.getKeyframeCoordinate().getYRot();
        this.rotationValue = keyframe.getKeyframeCoordinate().getZRot();
        this.fovValue = keyframe.getKeyframeCoordinate().getFov();
        this.playerSession = Utils.getSessionOrNull(player);
        this.hide = hide;
    }

    protected void init() {

    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void renderBlurredBackground() {}

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

    protected EditBox addLabeledEditBox(Component text, String defaultValue) {
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

    protected void initButtons() {}

    protected void initPositionLabelBox() {
        int currentX = INITIAL_POS_X;
        int editWidth = 50;
        int i = 0;
        KeyframeCoordinate position = keyframe.getKeyframeCoordinate();
        Double[] coords = {position.getX(), position.getY(), position.getZ()};
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
            box.setValue(String.format(Locale.US, "%.2f", coords[i]));
            this.addRenderableWidget(stringWidget);
            this.addRenderableWidget(box);
            coordinatesBoxList.add(box);
            currentX += stringWidget.getWidth() + editWidth + 10;
            i++;
        }
        currentY += 20;
    }

    protected void initTextSelectedKeyframe() {}

    protected void initLittleButtons() {}

    protected void initSliders() {
        int initialY = this.height - 50;
        int gap = 5;
        int numSliders = 4;
        int sliderWidth = (this.width - gap * (numSliders + 1)) / numSliders;
        int currentX = gap;

        Function<Float, String> formatFloat = val -> String.format(Locale.US, "%.2f", val);

        // === UP DOWN ===
        float defaultXRot = keyframe.getKeyframeCoordinate().getXRot();
        float defaultValXRot = defaultXRot + 90F;

        AbstractSliderButton upDownSlider = new AbstractSliderButton(currentX, initialY, sliderWidth, BUTTON_HEIGHT,
                Translation.message("screen.keyframe_option.up_down", formatFloat.apply(defaultValXRot)),
                defaultValXRot / 180F
        ) {
            @Override
            protected void updateMessage() {
                this.setMessage(Translation.message("screen.keyframe_option.up_down", formatFloat.apply(getValue() + 90F)));
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

        EditBox upDownBox = new EditBox(this.font, currentX, initialY + BUTTON_HEIGHT + 5, EDIT_BOX_WIDTH, EDIT_BOX_HEIGHT, Component.literal("Up Down Value"));
        upDownBox.setValue(formatFloat.apply(defaultValXRot));
        upDownBox.setFilter(s -> s.matches(Utils.REGEX_FLOAT_POSITIVE_ONLY));
        Button upDownButton = Button.builder(Component.literal("✔"), btn -> {
            upDownValue = Float.parseFloat(upDownBox.getValue()) - 90F;
            updateValues();
            keyframe.openScreenOption(player);
        }).bounds(currentX + EDIT_BOX_WIDTH + 5, upDownBox.getY(), 20, EDIT_BOX_HEIGHT).build();

        this.addRenderableWidget(upDownSlider);
        this.addRenderableWidget(upDownBox);
        this.addRenderableWidget(upDownButton);

        currentX += sliderWidth + gap;

        // === LEFT RIGHT ===
        float defaultYRot = MathUtils.get360Angle(keyframe.getKeyframeCoordinate().getYRot());

        AbstractSliderButton leftRightSlider = new AbstractSliderButton(currentX, initialY, sliderWidth, BUTTON_HEIGHT,
                Translation.message("screen.keyframe_option.left_right", formatFloat.apply(defaultYRot)),
                defaultYRot / 360F
        ) {
            @Override
            protected void updateMessage() {
                float value = MathUtils.get360Angle(keyframe.getKeyframeCoordinate().getYRot());
                if (value == 0F && this.value == 1F) value = 360F;
                this.setMessage(Translation.message("screen.keyframe_option.left_right", formatFloat.apply(value)));
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

        EditBox leftRightBox = new EditBox(this.font, currentX, initialY + BUTTON_HEIGHT + 5, EDIT_BOX_WIDTH, EDIT_BOX_HEIGHT, Component.literal("Left Right Value"));
        leftRightBox.setValue(formatFloat.apply(defaultYRot));
        leftRightBox.setFilter(s -> s.matches(Utils.REGEX_FLOAT_POSITIVE_ONLY));
        Button leftRightButton = Button.builder(Component.literal("✔"), btn -> {
            leftRightValue = Float.parseFloat(leftRightBox.getValue());
            updateValues();
            keyframe.openScreenOption(player);
        }).bounds(currentX + EDIT_BOX_WIDTH + 5, leftRightBox.getY(), 20, EDIT_BOX_HEIGHT).build();

        this.addRenderableWidget(leftRightSlider);
        this.addRenderableWidget(leftRightBox);
        this.addRenderableWidget(leftRightButton);

        currentX += sliderWidth + gap;

        // === ROTATION ===
        float defaultZRot = MathUtils.get180Angle(keyframe.getKeyframeCoordinate().getZRot());

        AbstractSliderButton rotationSlider = new AbstractSliderButton(currentX, initialY, sliderWidth, BUTTON_HEIGHT,
                Translation.message("screen.keyframe_option.rotation", defaultZRot),
                ((keyframe.getKeyframeCoordinate().getZRot() + 180F) % 360F) / 360F
        ) {
            @Override
            protected void updateMessage() {
                float angle = (float) (this.value * 360 - 180);
                this.setMessage(Translation.message("screen.keyframe_option.rotation", formatFloat.apply(angle)));
            }

            @Override
            protected void applyValue() {
                float angle = (float) (this.value * 360 - 180);
                rotationValue = (angle + 360) % 360;
                updateValues();
            }
        };

        EditBox rotationBox = new EditBox(this.font, currentX, initialY + BUTTON_HEIGHT + 5, EDIT_BOX_WIDTH, EDIT_BOX_HEIGHT, Component.literal("Rotation Value"));
        rotationBox.setValue(formatFloat.apply(defaultZRot));
        rotationBox.setFilter(s -> s.matches(Utils.REGEX_FLOAT));
        Button rotationButton = Button.builder(Component.literal("✔"), btn -> {
            if(rotationBox.getValue().equals("-")) return;
            float angle = Float.parseFloat(rotationBox.getValue());
            rotationValue = (angle + 360F) % 360F;
            updateValues();
            keyframe.openScreenOption(player);
        }).bounds(currentX + EDIT_BOX_WIDTH + 5, rotationBox.getY(), 20, EDIT_BOX_HEIGHT).build();

        this.addRenderableWidget(rotationSlider);
        this.addRenderableWidget(rotationBox);
        this.addRenderableWidget(rotationButton);

        currentX += sliderWidth + gap;

        // === FOV ===
        float defaultFov = keyframe.getKeyframeCoordinate().getFov();

        AbstractSliderButton fovSlider = new AbstractSliderButton(currentX, initialY, sliderWidth, BUTTON_HEIGHT,
                Translation.message("screen.keyframe_option.fov", formatFloat.apply(defaultFov)),
                defaultFov / 150F
        ) {
            @Override
            protected void updateMessage() {
                this.setMessage(Translation.message("screen.keyframe_option.fov", formatFloat.apply(getValue())));
            }

            @Override
            protected void applyValue() {
                fovValue = getValue();
                updateValues();
            }

            public float getValue() {
                return (float) (this.value * 150F);
            }
        };

        EditBox fovBox = new EditBox(this.font, currentX, initialY + BUTTON_HEIGHT + 5, EDIT_BOX_WIDTH, EDIT_BOX_HEIGHT, Component.literal("FOV Value"));
        fovBox.setValue(formatFloat.apply(defaultFov));
        fovBox.setFilter(s -> s.matches(Utils.REGEX_FLOAT_POSITIVE_ONLY));
        Button fovButton = Button.builder(Component.literal("✔"), btn -> {
            fovValue = Float.parseFloat(fovBox.getValue());
            updateValues();
            keyframe.openScreenOption(player);
        }).bounds(currentX + EDIT_BOX_WIDTH + 5, fovBox.getY(), 20, EDIT_BOX_HEIGHT).build();

        this.addRenderableWidget(fovSlider);
        this.addRenderableWidget(fovBox);
        this.addRenderableWidget(fovButton);
    }

    protected void updateValues() {}

    public boolean isHide() {
        return hide;
    }
}
