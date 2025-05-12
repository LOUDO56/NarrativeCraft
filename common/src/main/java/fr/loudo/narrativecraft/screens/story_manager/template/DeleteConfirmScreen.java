package fr.loudo.narrativecraft.screens.story_manager.template;

import fr.loudo.narrativecraft.narrative.StoryDetails;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class DeleteConfirmScreen extends Screen {

    private final Screen lastScreen;
    private final StoryDetails storyDetails;

    public DeleteConfirmScreen(Screen lastScreen, StoryDetails storyDetails) {
        super(Component.literal("Confirm Delete"));
        this.lastScreen = lastScreen;
        this.storyDetails = storyDetails;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int buttonWidth = 100;
        int buttonHeight = 20;
        int spacing = 10;

        this.addRenderableWidget(ScreenUtils.text(
                Component.literal("Are you sure? This cannot be undone!"),
                this.font,
                centerX - this.font.width("Are you sure? This cannot be undone!") / 2,
                centerY - 30
        ));

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> {
            this.onClose();
        }).bounds(centerX - spacing / 2 - buttonWidth, centerY + 10, buttonWidth, buttonHeight).build());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_YES, button -> {
            storyDetails.remove();
            this.onClose();
        }).bounds(centerX + spacing / 2, centerY + 10, buttonWidth, buttonHeight).build());
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(lastScreen);
    }
}
