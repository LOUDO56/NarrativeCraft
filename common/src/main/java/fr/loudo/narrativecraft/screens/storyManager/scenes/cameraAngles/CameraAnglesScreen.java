package fr.loudo.narrativecraft.screens.storyManager.scenes.cameraAngles;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.StoryDetails;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleGroup;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.screens.storyManager.scenes.ScenesMenuScreen;
import fr.loudo.narrativecraft.screens.storyManager.template.StoryElementList;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class CameraAnglesScreen extends StoryElementScreen {
    private final Scene scene;

    public CameraAnglesScreen(Scene scene) {
        super(null, Minecraft.getInstance().options, Translation.message("screen.camera_angles_manager.title", Component.literal(scene.getName()).withColor(StoryElementScreen.SCENE_NAME_COLOR)));
        this.scene = scene;
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
        for(CameraAngleGroup cameraAngleGroup : scene.getCameraAngleGroupList()) {
            Button button = Button.builder(Component.literal(String.valueOf(cameraAngleGroup.getName())), button1 -> {
                PlayerSession playerSession = NarrativeCraftMod.getInstance().getPlayerSessionManager().setSession(this.minecraft.player, scene.getChapter(), scene);
                CameraAngleController cameraAngleController = new CameraAngleController(cameraAngleGroup, Utils.getServerPlayerByUUID(this.minecraft.player.getUUID()));
                playerSession.setKeyframeControllerBase(cameraAngleController);
                cameraAngleController.startSession();
            }).build();
            buttons.add(button);
            storyDetails.add(cameraAngleGroup);
        }
        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, buttons, storyDetails));
    }

    public Scene getScene() {
        return scene;
    }
}
