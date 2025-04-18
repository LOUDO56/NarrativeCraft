package fr.loudo.narrativecraft.screens;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.utils.PlayerCoord;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.Util;
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

    private final int INITIAL_POS_X = 80;
    private final int INITIAL_POS_Y = 40;
    private final int EDIT_BOX_WIDTH = 60;
    private final int EDIT_BOX_HEIGHT = 15;
    private final int BUTTON_WIDTH = 60;
    private final int BUTTON_HEIGHT = 20;
    private final String REGEX_FLOAT = "[+-]?([0-9]+([.][0-9]*)?|[.][0-9]+)";

    private Keyframe keyframe;
    private int currentY = INITIAL_POS_Y;
    private List<EditBox> editBoxList;
    private AbstractSliderButton pitchSlider;
    private AbstractSliderButton yawSlider;

    private float pitchValue = 0F;
    private float yawValue = 0F;

    private ServerPlayer player;

    public KeyframeOptionScreen(Keyframe keyframe, ServerPlayer player) {
        super(Component.literal("Keyframe Option"));
        this.keyframe = keyframe;
        this.editBoxList = new ArrayList<>();
        this.player = player;
        this.pitchValue = keyframe.getPosition().getXRot();
        this.yawValue = keyframe.getPosition().getYRot();
    }

    @Override
    protected void init() {
        addLabeledEditBox(Translation.message("screen.keyframe.start_delay"), String.valueOf(Utils.getSecondsByMillis(keyframe.getStartDelay())));
        addLabeledEditBox(Translation.message("screen.keyframe.path_time"), String.valueOf(Utils.getSecondsByMillis(keyframe.getPathTime())));
        initPositionLabelBox();
        initSliders();
        initUpdateButton();

    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

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
        Button updateButton = Button.builder(Translation.message("screen.update"), button -> {
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
            position.setXRot(pitchValue);
            position.setYRot(yawValue);
            keyframe.setPosition(position);
            keyframe.updateItemData(player);
            player.sendSystemMessage(Translation.message("screen.keyframe.updated", keyframe.getId()), false);
            this.onClose();
        }).bounds(INITIAL_POS_X, currentY, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        currentY += 25;
        Button closeButton = Button.builder(Translation.message("screen.close"), button -> {
            this.onClose();
        }).bounds(INITIAL_POS_X, currentY, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        currentY += 25;
        Button removeKeyframe = Button.builder(Translation.message("screen.remove"), button -> {
            PlayerSession playerSession = Utils.getSessionOrNull(player);
            if(playerSession != null) {
                playerSession.getCutsceneController().removeKeyframe(keyframe);
                this.onClose();
            }
        }).bounds(INITIAL_POS_X, currentY, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(updateButton);
        this.addRenderableWidget(closeButton);
        this.addRenderableWidget(removeKeyframe);
    }

    private void initSliders() {
        Component curentPitchMessage = Component.literal(String.format("Pitch %.2f", keyframe.getPosition().getXRot()));
        pitchSlider = new AbstractSliderButton(INITIAL_POS_X, currentY + 30, 150, BUTTON_HEIGHT,
                curentPitchMessage,
                (Utils.get180Angle(keyframe.getPosition().getXRot()) + 90F) / 180F
        ) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal(String.format("Pitch %.2f", getValue())));
            }

            @Override
            protected void applyValue() {
                pitchValue = getValue();
            }

            public float getValue() {
                return (float)(this.value * 180F - 90F);
            }
        };

        Component currentYawMessage = Component.literal(String.format("Yaw %.2f", keyframe.getPosition().getYRot()));

        yawSlider = new AbstractSliderButton(INITIAL_POS_X, currentY + 60, 150, BUTTON_HEIGHT, currentYawMessage, Utils.get360Angle( keyframe.getPosition().getYRot()) / 360) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal(String.format("Yaw %.2f", getValue())));
            }

            @Override
            protected void applyValue() {
                yawValue = getValue();
            }

            public float getValue() {
                return Utils.get180Angle((float) (this.value * 360));
            }
        };

        this.addRenderableWidget(pitchSlider);
        this.addRenderableWidget(yawSlider);

        currentY += 90;

    }
}
