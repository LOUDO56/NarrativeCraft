package fr.loudo.narrativecraft.screens;

import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.Keyframe;
import fr.loudo.narrativecraft.utils.PlayerCoord;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

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

    public KeyframeOptionScreen(Keyframe keyframe) {
        super(Component.literal("Skibidi"));
        this.keyframe = keyframe;
        this.editBoxList = new ArrayList<>();
    }

    @Override
    protected void init() {
        addLabeledEditBox(Translation.message("screen.keyframe.start_delay"), String.valueOf(Utils.getSecondsByMillis(keyframe.getStartDelay())));
        addLabeledEditBox(Translation.message("screen.keyframe.path_time"), String.valueOf(Utils.getSecondsByMillis(keyframe.getPathTime())));
        addPositionLabelBox();
        addUpdateButton();
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

    private void addPositionLabelBox() {
        int currentX = INITIAL_POS_X;
        int editWidth = 50;
        int i = 0;
        PlayerCoord position = keyframe.getPosition();
        Float[] coords = {(float)position.getX(), (float)position.getY(), (float)position.getZ(), position.getXRot(), position.getYRot()};
        String[] labels = {"X:", "Y:", "Z:", "Yaw:", "Pitch:"};
        for(String label : labels) {
            StringWidget stringWidget = ScreenUtils.text(Component.literal(label), this.font, currentX, currentY);
            EditBox box = new EditBox(this.font,
                    currentX + stringWidget.getWidth() + 5,
                    currentY - stringWidget.getHeight() / 2,
                    editWidth,
                    EDIT_BOX_HEIGHT,
                    Component.literal(stringWidget + " Value"));
            box.setFilter(s -> s.matches(REGEX_FLOAT));
            System.out.println(coords[i]);
            box.setValue(String.format("%.2f", coords[i]));
            this.addRenderableWidget(stringWidget);
            this.addRenderableWidget(box);
            editBoxList.add(box);
            currentX += stringWidget.getWidth() + editWidth + 10;
            i++;
        }
    }

    private void addUpdateButton() {
        Button updateButton = Button.builder(Translation.message("screen.update"), button -> {
            float startDelayVal = Float.parseFloat((editBoxList.get(0).getValue()));
            float pathTimeVal = Float.parseFloat((editBoxList.get(1).getValue()));
            float xVal = Float.parseFloat((editBoxList.get(2).getValue()));
            float yVal = Float.parseFloat((editBoxList.get(3).getValue()));
            float zVal = Float.parseFloat((editBoxList.get(4).getValue()));
            float XRotVal = Float.parseFloat((editBoxList.get(5).getValue()));
            float YRotVal = Float.parseFloat((editBoxList.get(6).getValue()));
            PlayerCoord position = keyframe.getPosition();
            keyframe.setStartDelay(Utils.getMillisBySecond(startDelayVal));
            keyframe.setPathTime(Utils.getMillisBySecond(pathTimeVal));
            position.setX(xVal);
            position.setY(yVal);
            position.setZ(zVal);
            position.setXRot(XRotVal);
            position.setYRot(YRotVal);
            keyframe.setPosition(position);
            keyframe.updateItemPosition();
            Minecraft.getInstance().player.displayClientMessage(Translation.message("screen.keyframe.updated", keyframe.getId()), false);
            this.onClose();
        }).bounds(INITIAL_POS_X, currentY + 20, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        Button closeButton = Button.builder(Translation.message("screen.close"), button -> {
            this.onClose();
        }).bounds(INITIAL_POS_X, currentY + 60, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(updateButton);
        this.addRenderableWidget(closeButton);
    }
}
