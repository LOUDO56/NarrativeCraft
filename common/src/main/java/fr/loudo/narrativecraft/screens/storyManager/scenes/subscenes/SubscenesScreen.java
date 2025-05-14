package fr.loudo.narrativecraft.screens.storyManager.scenes.subscenes;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.StoryDetails;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.screens.storyManager.scenes.ScenesMenuScreen;
import fr.loudo.narrativecraft.screens.storyManager.template.PickElementScreen;
import fr.loudo.narrativecraft.screens.storyManager.template.StoryElementList;
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
            List<Animation> animationsAvailable = scene.getAnimationList().stream()
                    .filter(animation -> subscene.getAnimationList().stream()
                            .noneMatch(a -> a.getName().equals(animation.getName())))
                    .toList();
            Button button = Button.builder(Component.literal(String.valueOf(subscene.getName())), button1 -> {
                PickElementScreen screen = new PickElementScreen(
                    this,
                    Translation.message("screen.selector.subscene.title", Translation.message("global.animations"), Component.literal(subscene.getName()).withColor(StoryElementScreen.SUBSCENE_NAME_COLOR)),
                    Translation.message("global.animations"),
                    animationsAvailable,
                    subscene.getAnimationList(),
                    (entries) -> {
                        if(NarrativeCraftFile.subscenesFileExist(scene)) {
                            List<String> animationStringList = new ArrayList<>();
                            List<Animation> animationList = new ArrayList<>();
                            for(PickElementScreen.TransferableStorySelectionList.Entry entry : entries) {
                                animationStringList.add(entry.getStoryDetails().getName());
                                animationList.add((Animation) entry.getStoryDetails());
                            }
                            subscene.setAnimationList(animationList);
                            subscene.setAnimationNameList(animationStringList);
                            NarrativeCraftFile.updateSubsceneFile(scene);
                        }
                        this.minecraft.setScreen(new SubscenesScreen(scene));
                });
                this.minecraft.setScreen(screen);
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
