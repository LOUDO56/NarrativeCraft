package fr.loudo.narrativecraft.screens;

import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class CutsceneSettingsScreen extends Screen {

    private ServerPlayer serverPlayer;
    private CutsceneController cutsceneController;
    private EditBox numberInput;
    private Button updateButton;

    private final int INPUT_WIDTH = 100;
    private final int BUTTON_WIDTH = 60;
    private final int BUTTON_HEIGHT = 20;

    public CutsceneSettingsScreen(CutsceneController cutsceneController, ServerPlayer player) {
        super(Component.literal("Change Skip Second"));
        this.serverPlayer = player;
        this.cutsceneController = cutsceneController;
    }

    public CutsceneSettingsScreen() {
        super(Component.literal("Change Skip Second"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int inputX = centerX - (INPUT_WIDTH + 5 + BUTTON_WIDTH) / 2;

        numberInput = new EditBox(this.font, inputX, centerY - 20, INPUT_WIDTH, BUTTON_HEIGHT, Component.literal("Number"));
        numberInput.setFilter(s -> s.matches("\\d*"));
        numberInput.setMaxLength(10);
        this.addRenderableWidget(numberInput);

        int updateX = inputX + INPUT_WIDTH + 5;
        updateButton = Button.builder(Component.literal("Update"), button -> {
            String input = numberInput.getValue();
            if (!input.isEmpty()) {
                int value = Integer.parseInt(input);
                if(cutsceneController != null) {
                    this.onClose();
                    cutsceneController.setCurrentSkipCount(value);
                    serverPlayer.sendSystemMessage(Translation.message("cutscene.changed_time_skip_value", value));
                }
            }
        }).bounds(updateX, centerY - 20, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(updateButton);

        int[] values = {1, 5, 10, 15, 60};
        int buttonCount = values.length;
        int totalTopWidth = INPUT_WIDTH + 5 + BUTTON_WIDTH;
        int buttonWidth = 30;

        int totalButtonsWidth = buttonCount * buttonWidth;
        int spacing = (totalTopWidth - totalButtonsWidth) / (buttonCount - 1);

        for (int i = 0; i < buttonCount; i++) {
            int val = values[i];
            int x = inputX + i * (buttonWidth + spacing);
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
