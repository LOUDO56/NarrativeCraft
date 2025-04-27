package fr.loudo.narrativecraft.screens;

import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CutsceneChangeTimeSkipScreen extends Screen {

    private Screen lastScreen;
    private CutsceneController cutsceneController;
    private EditBox numberInput;
    private Button updateButton;
    private final int INPUT_WIDTH = 100;
    private final int BUTTON_WIDTH = 60;
    private final int BUTTON_HEIGHT = 20;

    public CutsceneChangeTimeSkipScreen(CutsceneController cutsceneController, Screen lastScreen) {
        super(Component.literal("Change Time Skip Screen"));
        this.cutsceneController = cutsceneController;
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int inputX = centerX - (INPUT_WIDTH + 5 + BUTTON_WIDTH) / 2;

        numberInput = new EditBox(this.font, inputX, centerY - 20, INPUT_WIDTH + 20, BUTTON_HEIGHT, Component.literal("Number"));
        numberInput.setFilter(s -> s.matches(Utils.REGEX_FLOAT_POSITIVE_ONLY));
        numberInput.setMaxLength(10);
        this.addRenderableWidget(numberInput);

        int updateX = inputX + INPUT_WIDTH + 30;
        updateButton = Button.builder(Translation.message("screen.keyframe_option.update"), button -> {
            String input = numberInput.getValue();
            if (!input.isEmpty()) {
                double value = Double.parseDouble(input);
                cutsceneController.setCurrentSkipCount(value);
                Minecraft.getInstance().player.displayClientMessage(Translation.message("cutscene.changed_time_skip_value", value), false);
                if(lastScreen == null) {
                    this.onClose();
                } else {
                    Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(lastScreen));
                }
            }
        }).bounds(updateX, centerY - 20, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(updateButton);

        double[] values = {0.5, 1, 5, 10, 15, 60};
        int buttonCount = values.length;
        int totalTopWidth = INPUT_WIDTH + 5 + BUTTON_WIDTH;
        int buttonWidth = 30;

        int totalButtonsWidth = buttonCount * buttonWidth;
        int spacing = (totalTopWidth - totalButtonsWidth) / (buttonCount - 1);

        for (int i = 0; i < buttonCount; i++) {
            double val = values[i];
            int x = inputX + i * (buttonWidth + spacing + 5);
            Button b = Button.builder(Component.literal(String.valueOf(val)), button -> {
                numberInput.setValue(String.valueOf(val));
            }).bounds(x, centerY + 10, buttonWidth, BUTTON_HEIGHT).build();
            this.addRenderableWidget(b);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }


}
