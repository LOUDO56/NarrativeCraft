package fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.StoryDetails;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;

import java.util.ArrayList;
import java.util.List;

public class CameraAngleGroup extends StoryDetails {

    private transient Scene scene;
    List<CameraAngle> cameraAngleList;
    List<Character> characterList;

    public CameraAngleGroup(String name, String description) {
        super(name, description);
        cameraAngleList = new ArrayList<>();
        characterList = new ArrayList<>();
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

    public void setCameraAngleList(List<CameraAngle> cameraAngleList) {
        this.cameraAngleList = cameraAngleList;
    }

    public List<Character> getCharacterList() {
        return characterList;
    }

    public void setCharacterList(List<Character> characterList) {
        this.characterList = characterList;
    }

    @Override
    public void remove() {
        scene.removeCameraAnglesGroup(this);
        NarrativeCraftFile.updateCameraAnglesFile(scene);
    }
}
