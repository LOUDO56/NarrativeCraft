package fr.loudo.narrativecraft.screens.storyManager.components;

import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.gui.screens.Screen;

public class EditCharacterInfoScreen extends EditInfoScreen {

    private final int EDIT_BOX_AGE_HEIGHT = 10;
    private ScreenUtils.LabelBox ageBox;

    public EditCharacterInfoScreen(Screen lastScreen) {
        super(lastScreen);
    }

    public EditCharacterInfoScreen(Screen lastScreen, NarrativeEntry narrativeEntry) {
        super(lastScreen, narrativeEntry);
    }

    @Override
    protected void init() {
        super.init();

        int labelHeight = this.font.lineHeight + 5;

        int centerX = this.width / 2 - WIDGET_WIDTH / 2;
        int centerY = this.height / 2 - (labelHeight + EDIT_BOX_NAME_HEIGHT + GAP + labelHeight + EDIT_BOX_DESCRIPTION_HEIGHT + labelHeight + GAP + EDIT_BOX_AGE_HEIGHT + (BUTTON_HEIGHT * 2)) / 2;
        titleWidget.setPosition(titleWidget.getX(), centerY - labelHeight);

        nameBox.setPosition(centerX, centerY);

        centerY += labelHeight + EDIT_BOX_NAME_HEIGHT + GAP;
        descriptionBox.setPosition(centerX, centerY);

        centerY += labelHeight + EDIT_BOX_DESCRIPTION_HEIGHT + GAP;
        ageBox = new ScreenUtils.LabelBox(
                Translation.message("screen.characters_manager.age"),
                font,
                30,
                EDIT_BOX_NAME_HEIGHT,
                centerX,
                centerY,
                ScreenUtils.Align.HORIZONTAL
        );
        ageBox.getEditBox().setFilter(s -> s.matches("^[0-9]*$"));
        this.addRenderableWidget(ageBox.getStringWidget());
        this.addRenderableWidget(ageBox.getEditBox());

        centerY += EDIT_BOX_AGE_HEIGHT + GAP + 10;
        actionButton.setPosition(centerX, centerY);

        centerY += actionButton.getHeight() + GAP;
        backButton.setPosition(centerX, centerY);

    }
}
