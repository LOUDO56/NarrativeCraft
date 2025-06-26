package fr.loudo.narrativecraft.screens.storyManager.scenes.animations;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.screens.animations.AnimationCharacterLinkScreen;
import fr.loudo.narrativecraft.screens.components.ChangeSkinLinkScreen;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.screens.storyManager.scenes.ScenesMenuScreen;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.utils.ImageFontConstants;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.util.List;

public class AnimationsScreen extends StoryElementScreen {

    private final Scene scene;

    public AnimationsScreen(Scene scene) {
        super(null, Minecraft.getInstance().options, Translation.message("screen.animation_manager.title", Component.literal(scene.getName()).withColor(StoryElementScreen.SCENE_NAME_COLOR)));
        this.scene = scene;
    }

    @Override
    protected void addTitle() {
        LinearLayout linearlayout = this.layout.addToHeader(LinearLayout.horizontal()).spacing(8);
        linearlayout.defaultCellSetting().alignVerticallyMiddle();
        linearlayout.addChild(new StringWidget(this.title, this.font));
        linearlayout.addChild(Button.builder(ImageFontConstants.FOLDER, button -> {
            openFolder();
        }).width(25).build());

    }

    @Override
    protected void openFolder() {
        Util.getPlatform().openPath(NarrativeCraftFile.animationFolder(scene).toPath());
    }

    @Override
    public void onClose() {
        ScenesMenuScreen screen = new ScenesMenuScreen(scene);
        this.minecraft.setScreen(screen);
    }

    @Override
    public void addContents() {
        List<StoryElementList.StoryEntryData> entries = scene.getAnimationList().stream()
                .map(animation -> {

                    Button settingsButton = createSettingsButton(animation);

                    Button button = Button.builder(Component.literal(animation.getName()), b -> {
                        // TODO: preview animation
                    }).build();
                    Button changeSkin = Button.builder(ImageFontConstants.CHARACTER, b -> {
                        ChangeSkinLinkScreen changeSkinLinkScreen = new ChangeSkinLinkScreen(this, animation.getCharacter(), skin -> {
                            animation.setSkinName(skin);
                            NarrativeCraftFile.updateAnimationFile(animation);
                        });
                        minecraft.setScreen(changeSkinLinkScreen);
                    }).build();
                    return new StoryElementList.StoryEntryData(button, animation, List.of(settingsButton, changeSkin));
                }).toList();
        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, entries));
    }

    private Button createSettingsButton(Animation animation) {

        return Button.builder(ImageFontConstants.SETTINGS, button1 -> {
            AnimationCharacterLinkScreen screen = new AnimationCharacterLinkScreen(this, animation);
            this.minecraft.setScreen(screen);
        }).build();

    }

    public Scene getScene() {
        return scene;
    }
}
