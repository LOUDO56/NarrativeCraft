package fr.loudo.narrativecraft.screens.storyManager.scenes.cameraAngles;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleController;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.screens.storyManager.scenes.ScenesMenuScreen;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

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
        List<StoryElementList.StoryEntryData> entries = scene.getCameraAngleGroupList().stream()
                .map(group -> {
                    Button button = Button.builder(Component.literal(group.getName()), b -> {
                        PlayerSession session = NarrativeCraftMod.getInstance().getPlayerSessionManager().setSession(this.minecraft.player, scene.getChapter(), scene);
                        CameraAngleController controller = new CameraAngleController(group, Utils.getServerPlayerByUUID(this.minecraft.player.getUUID()), Playback.PlaybackType.DEVELOPMENT);
                        session.setKeyframeControllerBase(controller);
                        controller.startSession();
                    }).build();
                    return new StoryElementList.StoryEntryData(button, group);
                }).toList();
        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, entries));
    }

    public Scene getScene() {
        return scene;
    }
}
