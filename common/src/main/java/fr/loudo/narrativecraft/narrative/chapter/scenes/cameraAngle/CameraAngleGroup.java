package fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeGroup;
import fr.loudo.narrativecraft.screens.storyManager.scenes.cameraAngles.CameraAnglesScreen;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;

public class CameraAngleGroup extends NarrativeEntry {

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

    public CameraAngle getCameraAngleByName(String name) {
        for(CameraAngle cameraAngle : cameraAngleList) {
            if(cameraAngle.getName().equals(name)) {
                return cameraAngle;
            }
        }
        return null;
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
    public void update(String name, String description) {
        String oldName = this.name;
        String oldDescription = this.description;
        this.name = name;
        this.description = description;
        if(!NarrativeCraftFile.updateCameraAnglesFile(scene)) {
            this.name = oldName;
            this.description = oldDescription;
            ScreenUtils.sendToast(Translation.message("toast.error"), Translation.message("screen.camera_angles_manager.update.failed", name));
            return;
        }
        ScreenUtils.sendToast(Translation.message("toast.info"), Translation.message("toast.description.updated"));
        Minecraft.getInstance().setScreen(reloadScreen());
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
