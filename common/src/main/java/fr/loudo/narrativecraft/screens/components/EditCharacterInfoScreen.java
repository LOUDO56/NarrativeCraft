package fr.loudo.narrativecraft.screens.components;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.screens.storyManager.characters.CharactersScreen;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.time.LocalDate;

public class EditCharacterInfoScreen extends EditInfoScreen {

    private final int EDIT_BOX_BIRTHDATE_HEIGHT = 20;
    private final int EDIT_BOX_BIRTHDATE_WIDTH = 20;
    private ScreenUtils.LabelBox dayBox, monthBox, yearBox;
    private String defaultBirthdate;

    public EditCharacterInfoScreen(Screen lastScreen) {
        super(lastScreen);
    }

    public EditCharacterInfoScreen(Screen lastScreen, CharacterStory characterStory) {
        super(lastScreen, characterStory);
        defaultBirthdate = characterStory.getBirthdate();
    }

    @Override
    protected void init() {
        super.init();

        int labelHeight = this.font.lineHeight + 5;

        int centerX = this.width / 2 - WIDGET_WIDTH / 2;
        int centerY = this.height / 2 - (labelHeight + EDIT_BOX_NAME_HEIGHT + GAP + labelHeight + EDIT_BOX_DESCRIPTION_HEIGHT + labelHeight + GAP + EDIT_BOX_BIRTHDATE_HEIGHT + (BUTTON_HEIGHT * 2)) / 2;
        titleWidget.setPosition(titleWidget.getX(), centerY - labelHeight);

        nameBox.setPosition(centerX, centerY);

        centerY += labelHeight + EDIT_BOX_NAME_HEIGHT + GAP;
        descriptionBox.setPosition(centerX, centerY);

        centerY += labelHeight + EDIT_BOX_DESCRIPTION_HEIGHT + GAP;

        StringWidget birthDateString = ScreenUtils.text(
                Component.literal("Birthdate"),
                font,
                centerX,
                centerY
        );
        this.addRenderableWidget(birthDateString);

        centerY += birthDateString.getHeight() + GAP;
        LocalDate localDate = LocalDate.now();

        String defaultDay, defaultMonth, defaultYear;
        if(defaultBirthdate == null) {
            defaultDay = String.valueOf(localDate.getDayOfMonth());
            defaultMonth = String.valueOf(localDate.getMonthValue());
            defaultYear = "2000";
        } else {
            String[] splitBirthdate = defaultBirthdate.split("/");
            defaultDay = splitBirthdate[0];
            defaultMonth = splitBirthdate[1];
            defaultYear = splitBirthdate[2];
        }

        dayBox = new ScreenUtils.LabelBox(
                Component.literal("Day"),
                font,
                EDIT_BOX_BIRTHDATE_WIDTH,
                EDIT_BOX_BIRTHDATE_HEIGHT,
                centerX,
                centerY,
                ScreenUtils.Align.HORIZONTAL
        );
        dayBox.getEditBox().setValue(defaultDay);
        this.addRenderableWidget(dayBox.getStringWidget());
        this.addRenderableWidget(dayBox.getEditBox());

        monthBox = new ScreenUtils.LabelBox(
                Component.literal("Month"),
                font,
                EDIT_BOX_BIRTHDATE_WIDTH,
                EDIT_BOX_BIRTHDATE_HEIGHT,
                dayBox.getEditBox().getX() + dayBox.getEditBox().getWidth() + 10,
                centerY,
                ScreenUtils.Align.HORIZONTAL
        );
        monthBox.getEditBox().setValue(defaultMonth);
        this.addRenderableWidget(monthBox.getStringWidget());
        this.addRenderableWidget(monthBox.getEditBox());

        yearBox = new ScreenUtils.LabelBox(
                Component.literal("Year"),
                font,
                EDIT_BOX_BIRTHDATE_WIDTH + 12,
                EDIT_BOX_BIRTHDATE_HEIGHT,
                monthBox.getEditBox().getX() + monthBox.getEditBox().getWidth() + 10,
                centerY,
                ScreenUtils.Align.HORIZONTAL
        );
        yearBox.getEditBox().setValue(defaultYear);
        this.addRenderableWidget(yearBox.getStringWidget());
        this.addRenderableWidget(yearBox.getEditBox());

        centerY += EDIT_BOX_BIRTHDATE_HEIGHT + GAP;
        Component buttonActionMessage = narrativeEntry == null ? Translation.message("screen.add.text") : Translation.message("screen.update.text");
        this.removeWidget(actionButton);
        actionButton = Button.builder(buttonActionMessage, button -> {
            String name = nameBox.getEditBox().getValue();
            String desc = descriptionBox.getMultiLineEditBox().getValue();
            String day = dayBox.getEditBox().getValue();
            String month = monthBox.getEditBox().getValue();
            String year = yearBox.getEditBox().getValue();
            if(name.isEmpty()) {
                ScreenUtils.sendToast(Translation.message("global.error"), Translation.message("screen.story.name.required"));
                return;
            }
            if(narrativeEntry == null) {
                addCharacter(name, desc, day, month, year);
            } else {
                ((CharacterStory)narrativeEntry).update(name, desc, day, month, year);
            }
        }).bounds(centerX, centerY, WIDGET_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(actionButton);

        centerY += actionButton.getHeight() + GAP;
        backButton.setPosition(centerX, centerY);

    }

    private void addCharacter(String name, String desc, String day, String month, String year) {
        if(NarrativeCraftMod.getInstance().getCharacterManager().characterExists(name)) {
            ScreenUtils.sendToast(Translation.message("global.error"), Translation.message("screen.characters_manager.add.already_exists"));
            return;
        }
        CharacterStory characterStory = new CharacterStory(
                name,
                desc,
                day,
                month,
                year
        );
        if(!NarrativeCraftFile.updateCharacterFile(characterStory)) {
            ScreenUtils.sendToast(Translation.message("global.error"), Translation.message("screen.characters_manager.add.failed", name));
            return;
        }
        NarrativeCraftMod.getInstance().getCharacterManager().addCharacter(characterStory);
        CharactersScreen screen = new CharactersScreen();
        this.minecraft.setScreen(screen);
    }
}
