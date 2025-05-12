package fr.loudo.narrativecraft.screens.story_manager.scenes.subscenes;

import fr.loudo.narrativecraft.narrative.StoryDetails;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;
import fr.loudo.narrativecraft.screens.story_manager.StoryElementScreen;
import fr.loudo.narrativecraft.screens.story_manager.scenes.ScenesMenuScreen;
import fr.loudo.narrativecraft.screens.story_manager.template.StoryElementList;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class SubscenesScreen extends StoryElementScreen {

    private final Scene scene;

    public SubscenesScreen(Scene scene) {
        super(null, Minecraft.getInstance().options, Translation.message("screen.subscene_manager.title", Component.literal(scene.getName()).withColor(0x5896ED)));
        this.scene = scene;
    }

    @Override
    public void onClose() {
        ScenesMenuScreen screen = new ScenesMenuScreen(scene);
        this.minecraft.setScreen(screen);
    }

    @Override
    protected void addContents() {
        List<Button> buttons = new ArrayList<>();
        List<StoryDetails> storyDetails = new ArrayList<>();
        for(Subscene subscene : scene.getSubsceneList()) {
            Button button = Button.builder(Component.literal(String.valueOf(subscene.getName())), button1 -> {
                //TODO: show a screen where we can add animations
            }).build();
            buttons.add(button);
            storyDetails.add(subscene);
        }
        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, buttons, storyDetails));
    }

    public Scene getScene() {
        return scene;
    }
}
