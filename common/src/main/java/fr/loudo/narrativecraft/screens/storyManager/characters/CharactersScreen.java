package fr.loudo.narrativecraft.screens.storyManager.characters;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.character.CharacterManager;
import fr.loudo.narrativecraft.screens.characters.CharacterEntityTypeScreen;
import fr.loudo.narrativecraft.screens.components.EditCharacterInfoScreen;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.utils.ImageFontConstants;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.List;

public class CharactersScreen extends StoryElementScreen {

    public CharactersScreen() {
        super(null, Minecraft.getInstance().options, Translation.message("screen.characters_manager.title"));
    }

    @Override
    protected void addTitle() {
        LinearLayout linearlayout = this.layout.addToHeader(LinearLayout.horizontal()).spacing(8);
        linearlayout.defaultCellSetting().alignVerticallyMiddle();
        linearlayout.addChild(new StringWidget(this.title, this.font));
        linearlayout.addChild(Button.builder(ImageFontConstants.ADD, button -> {
            EditInfoScreen screen = new EditCharacterInfoScreen(this);
            this.minecraft.setScreen(screen);
        }).width(25).build());
        linearlayout.addChild(Button.builder(ImageFontConstants.FOLDER, button -> {
            openFolder();
        }).width(25).build());
    }

    @Override
    protected void addFooter() {
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, (p_345997_) -> this.onClose()).width(200).build());
    }

    @Override
    protected void openFolder() {
        Util.getPlatform().openPath(NarrativeCraftFile.characterDirectory.toPath());
    }

    @Override
    protected void addContents() {
        CharacterManager characterManager = NarrativeCraftMod.getInstance().getCharacterManager();
        List<StoryElementList.StoryEntryData> entries = characterManager.getCharacterStories().stream()
                .map(characterStory -> {
                    Button button = Button.builder(Component.literal(characterStory.getName()), b -> {

                    }).build();
                    button.active = false;

                    Button entityTypeButton = Button.builder(ImageFontConstants.ENTITY, button1 -> {
                        CharacterEntityTypeScreen screen = new CharacterEntityTypeScreen(this, characterStory);
                        minecraft.setScreen(screen);
                    }).build();

                    return new StoryElementList.StoryEntryData(button, characterStory, List.of(entityTypeButton));
                })
                .toList();

        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, entries, true));
    }
}
