package fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.StoryDetails;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeGroup;
import fr.loudo.narrativecraft.screens.storyManager.scenes.cameraAngles.CameraAnglesScreen;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;

public class CameraAngleGroup extends StoryDetails {

    private transient Scene scene;
    List<CameraAngle> cameraAngleList;
    List<CameraAngleCharacterPosition> characterPositions;

    public CameraAngleGroup(Scene scene, String name, String description) {
        super(name, description);
        this.scene = scene;
        cameraAngleList = new ArrayList<>();
        characterPositions = new ArrayList<>();
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public List<CameraAngle> getCameraAngleList() {
        return cameraAngleList;
    }

    public KeyframeGroup getCameraAngleListAsKeyframeGroup() {
        KeyframeGroup keyframeGroup = new KeyframeGroup(1);
        for(CameraAngle cameraAngle : cameraAngleList) {
            keyframeGroup.getKeyframeList().add(cameraAngle);
        }
        return keyframeGroup;
    }

    public void setCameraAngleList(List<CameraAngle> cameraAngleList) {
        this.cameraAngleList = cameraAngleList;
    }

    public List<CameraAngleCharacterPosition> getCharacterPositions() {
        return characterPositions;
    }

    public void setCharacterPositions(List<CameraAngleCharacterPosition> characterPositions) {
        this.characterPositions = characterPositions;
    }

    @Override
    public void remove() {
        scene.removeCameraAnglesGroup(this);
        NarrativeCraftFile.updateCameraAnglesFile(scene);
    }

    @Override
    public Screen reloadScreen() {
        return new CameraAnglesScreen(scene);
    }
}
