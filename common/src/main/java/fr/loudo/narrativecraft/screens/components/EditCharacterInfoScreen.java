package fr.loudo.narrativecraft.screens.components;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.screens.storyManager.characters.CharactersScreen;
import fr.loudo.narrativecraft.screens.storyManager.scenes.npcs.NpcScreen;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.PlayerSkin;
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
    public void onClose() {
        if(lastScreen instanceof NpcScreen) {
            NpcScreen npcScreen = new NpcScreen(((NpcScreen)lastScreen).getScene());
            minecraft.setScreen(npcScreen);
        } else {
            super.onClose();
        }
    }

    @Override
    protected void init() {
        super.init();

        int labelHeight = this.font.lineHeight + 5;

        int centerX = this.width / 2 - WIDGET_WIDTH / 2;
        int centerY = 0;
        if(lastScreen instanceof NpcScreen) {
            centerY = this.height / 2 - (labelHeight + EDIT_BOX_NAME_HEIGHT) / 2;
        } else {
            centerY = this.height / 2 - (labelHeight + EDIT_BOX_NAME_HEIGHT + GAP + labelHeight + EDIT_BOX_DESCRIPTION_HEIGHT + labelHeight + GAP + EDIT_BOX_BIRTHDATE_HEIGHT + (BUTTON_HEIGHT * 2)) / 2;
        }
        if(narrativeEntry != null) {
            centerY -= 10 + GAP;
        }
        titleWidget.setPosition(titleWidget.getX(), centerY - labelHeight);

        nameBox.setPosition(centerX, centerY);
        centerY += labelHeight + EDIT_BOX_NAME_HEIGHT + GAP;

        if(lastScreen instanceof CharactersScreen) {

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
        } else {
            descriptionBox.setPosition(-1000, -1000);
        }

        if(narrativeEntry != null) {
            CharacterStory characterStory = (CharacterStory)narrativeEntry;
            StringWidget modelText = ScreenUtils.text(Component.literal("Model"), minecraft.font, centerX, centerY + 6);
            Button modelButton = Button.builder(Component.literal(characterStory.getModel().name()), button -> {
                if(characterStory.getModel() == PlayerSkin.Model.WIDE) {
                    characterStory.setModel(PlayerSkin.Model.SLIM);
                    button.setMessage(Component.literal(PlayerSkin.Model.SLIM.name()));
                } else {
                    characterStory.setModel(PlayerSkin.Model.WIDE);
                    button.setMessage(Component.literal(PlayerSkin.Model.WIDE.name()));
                }
            }).pos(centerX + modelText.getWidth() + 5, centerY).width(70).build();

            this.addRenderableWidget(modelText);
            this.addRenderableWidget(modelButton);
            centerY += modelButton.getHeight() + GAP;
        }

        Component buttonActionMessage = narrativeEntry == null ? Translation.message("screen.add.text") : Translation.message("screen.update.text");
        this.removeWidget(actionButton);
        actionButton = Button.builder(buttonActionMessage, button -> {
            String name = nameBox.getEditBox().getValue();
            String desc = "";
            String day = "";
            String month = "";
            String year = "";
            if(lastScreen instanceof CharactersScreen) {
                desc = descriptionBox.getMultiLineEditBox().getValue();
                day = dayBox.getEditBox().getValue();
                month = monthBox.getEditBox().getValue();
                year = yearBox.getEditBox().getValue();
            }
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
        CharacterStory.CharacterType characterType = null;
        if(lastScreen instanceof CharactersScreen) {
            if(NarrativeCraftMod.getInstance().getCharacterManager().characterExists(name)) {
                ScreenUtils.sendToast(Translation.message("global.error"), Translation.message("screen.characters_manager.add.already_exists"));
                return;
            }
            characterType = CharacterStory.CharacterType.MAIN;
        } else if(lastScreen instanceof NpcScreen) {
            Scene scene = ((NpcScreen)lastScreen).getScene();
            if(scene.npcExists(name)) {
                ScreenUtils.sendToast(Translation.message("global.error"), Translation.message("screen.characters_manager.add.already_exists"));
                return;
            }
            characterType = CharacterStory.CharacterType.NPC;
        }
        CharacterStory characterStory = new CharacterStory(
                name,
                desc,
                PlayerSkin.Model.WIDE,
                characterType,
                day,
                month,
                year
        );
        StoryElementScreen screen = null;
        if(lastScreen instanceof CharactersScreen) {
            if(!NarrativeCraftFile.createCharacterFile(characterStory)) {
                ScreenUtils.sendToast(Translation.message("global.error"), Translation.message("screen.characters_manager.add.failed", name));
                return;
            }
            NarrativeCraftMod.getInstance().getCharacterManager().addCharacter(characterStory);
            screen = new CharactersScreen();
        } else if(lastScreen instanceof NpcScreen) {
            Scene scene = ((NpcScreen)lastScreen).getScene();
            characterStory.setScene(scene);
            if(!NarrativeCraftFile.createCharacterFileScene(characterStory, scene)) {
                ScreenUtils.sendToast(Translation.message("global.error"), Translation.message("screen.characters_manager.add.failed", name));
                return;
            }
            scene.addNpc(characterStory);
            screen = new NpcScreen(scene);
        }
        this.minecraft.setScreen(screen);
    }
}
