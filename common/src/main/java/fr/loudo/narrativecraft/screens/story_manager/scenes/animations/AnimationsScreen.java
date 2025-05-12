package fr.loudo.narrativecraft.screens.story_manager.scenes.animations;

import fr.loudo.narrativecraft.narrative.StoryDetails;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.screens.story_manager.StoryElementScreen;
import fr.loudo.narrativecraft.screens.story_manager.scenes.ScenesMenuScreen;
import fr.loudo.narrativecraft.screens.story_manager.template.StoryElementList;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class AnimationsScreen extends StoryElementScreen {

    private final Scene scene;

    public AnimationsScreen(Scene scene) {
        super(null, Minecraft.getInstance().options, Translation.message("screen.animation_manager.title", Component.literal(scene.getName()).withColor(0x5896ED)));
        this.scene = scene;
    }

    @Override
    protected void addTitle() {
        LinearLayout linearlayout = this.layout.addToHeader(LinearLayout.horizontal()).spacing(8);
        linearlayout.defaultCellSetting().alignVerticallyMiddle();
        linearlayout.addChild(new StringWidget(this.title, this.font));
    }

    @Override
    public void onClose() {
        ScenesMenuScreen screen = new ScenesMenuScreen(scene);
        this.minecraft.setScreen(screen);
    }

    @Override
    public void addContents() {
        List<Button> buttons = new ArrayList<>();
        List<StoryDetails> storyDetails = new ArrayList<>();
        for(Animation animation : scene.getAnimationList()) {
            Button button = Button.builder(Component.literal(String.valueOf(scene.getName())), button1 -> {
                //TODO: preview animation
            }).build();
            buttons.add(button);
            storyDetails.add(animation);
        }
        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, buttons, storyDetails));
    }

    public Scene getScene() {
        return scene;
    }
}
