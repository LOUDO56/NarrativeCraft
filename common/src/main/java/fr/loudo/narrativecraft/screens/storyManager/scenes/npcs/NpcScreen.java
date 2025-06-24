package fr.loudo.narrativecraft.screens.storyManager.scenes.npcs;

import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.screens.characters.CharacterEntityTypeScreen;
import fr.loudo.narrativecraft.screens.components.EditCharacterInfoScreen;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.screens.storyManager.scenes.ScenesMenuScreen;
import fr.loudo.narrativecraft.screens.storyManager.scenes.ScenesScreen;
import fr.loudo.narrativecraft.utils.ImageFontConstants;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.List;

public class NpcScreen extends StoryElementScreen {

    private final Scene scene;

    public NpcScreen(Scene scene) {
        super(null, Minecraft.getInstance().options, Translation.message("screen.npc_manager.title", Component.literal(scene.getName()).withColor(StoryElementScreen.SCENE_NAME_COLOR)));
        this.scene = scene;
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
    }

    @Override
    protected void addFooter() {
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_BACK, (p_345997_) -> this.onClose()).width(200).build());
    }

    @Override
    public void onClose() {
        ScenesMenuScreen screen = new ScenesMenuScreen(scene);
        this.minecraft.setScreen(screen);
    }

    @Override
    protected void addContents() {
        List<StoryElementList.StoryEntryData> entries = scene.getNpcs().stream()
                .map(npc -> {
                    Button button = Button.builder(Component.literal(npc.getName()), b -> {

                    }).build();
                    button.active = false;

                    Button entityTypeButton = Button.builder(Component.literal("T"), button1 -> {
                        CharacterEntityTypeScreen screen = new CharacterEntityTypeScreen(this, npc);
                        minecraft.setScreen(screen);
                    }).build();

                    return new StoryElementList.StoryEntryData(button, npc, List.of(entityTypeButton));
                })
                .toList();

        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, entries));
    }

    public Scene getScene() {
        return scene;
    }
}
