package fr.loudo.narrativecraft.screens.story_manager.scenes.cutscenes;

import fr.loudo.narrativecraft.narrative.StoryDetails;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.Cutscene;
import fr.loudo.narrativecraft.screens.story_manager.StoryElementScreen;
import fr.loudo.narrativecraft.screens.story_manager.scenes.ScenesMenuScreen;
import fr.loudo.narrativecraft.screens.story_manager.template.StoryElementList;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class CutscenesScreen extends StoryElementScreen {

    private final Scene scene;

    public CutscenesScreen(Scene scene) {
        super(null, Minecraft.getInstance().options, Translation.message("screen.cutscene_manager.title", Component.literal(scene.getName()).withColor(0x5896ED)));
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
        for(Cutscene cutscene : scene.getCutsceneList()) {
            Button button = Button.builder(Component.literal(String.valueOf(cutscene.getName())), button1 -> {
                //TODO: Open screen where we can add subscenes or animations
            }).build();
            buttons.add(button);
            storyDetails.add(cutscene);
        }
        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, buttons, storyDetails));
    }

    public Scene getScene() {
        return scene;
    }

}
