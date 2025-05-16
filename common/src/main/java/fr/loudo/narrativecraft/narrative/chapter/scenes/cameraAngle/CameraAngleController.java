package fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle;

import fr.loudo.narrativecraft.narrative.chapter.scenes.KeyframeControllerBase;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeCoordinate;
import fr.loudo.narrativecraft.screens.cameraAngles.CameraAngleControllerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public class CameraAngleController extends KeyframeControllerBase {

    public CameraAngleController(CameraAngleGroup cameraAngleGroup, ServerPlayer player) {
        super(cameraAngleGroup.getCameraAngleListAsKeyframeGroup(), player);
    }

    public void startSession() {

        for(Keyframe keyframe : keyframeGroups.getFirst().getKeyframeList()) {
            keyframe.showKeyframeToClient(player);
            keyframesEntity.add(keyframe.getCameraEntity());
        }

        player.setGameMode(GameType.SPECTATOR);
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(new CameraAngleControllerScreen(this)));

    }

    @Override
    public void stopSession() {
        for(Keyframe keyframe : keyframeGroups.getFirst().getKeyframeList()) {
            keyframe.removeKeyframeFromClient(player);
        }

        player.setGameMode(GameType.CREATIVE);
    }

    @Override
    public boolean addKeyframe() {
        return false;
    }

    public void addKeyframe(String name) {
        KeyframeCoordinate keyframeCoordinate = new KeyframeCoordinate(
                player.getX(),
                player.getY() + player.getEyeHeight(),
                player.getZ(),
                player.getXRot(),
                player.getYRot(),
                Minecraft.getInstance().options.fov().get()
        );
        CameraAngle cameraAngle = new CameraAngle(keyframeGroups.getFirst().getKeyframeList().size(), keyframeCoordinate, name);
        cameraAngle.showKeyframeToClient(player);
        keyframeGroups.getFirst().getKeyframeList().add(cameraAngle);
        updateKeyframeEntityName();

    }

    public void editKeyframe(CameraAngle cameraAngle, String value) {
        cameraAngle.setName(value);
        updateKeyframeEntityName();
    }

    private void updateKeyframeEntityName() {
        for(Keyframe keyframe : keyframeGroups.getFirst().getKeyframeList()) {
            CameraAngle cameraAngle = (CameraAngle) keyframe;
            keyframe.getCameraEntity().setCustomName(Component.literal(cameraAngle.getName()));
            keyframe.getCameraEntity().setCustomNameVisible(true);
            keyframe.updateEntityData(player);
        }
    }

    public void setCurrentPreviewKeyframe(Keyframe currentPreviewKeyframe) {
        this.currentPreviewKeyframe = currentPreviewKeyframe;
        currentPreviewKeyframe.openScreenOption(player);
        for(Keyframe keyframe : keyframeGroups.getFirst().getKeyframeList()) {
            keyframe.removeKeyframeFromClient(player);
        }
    }

    public void clearCurrentPreviewKeyframe() {
        Minecraft.getInstance().options.hideGui = false;
        for(Keyframe keyframe : keyframeGroups.getFirst().getKeyframeList()) {
            keyframe.showKeyframeToClient(player);
            updateKeyframeEntityName();
        }
        currentPreviewKeyframe = null;
    }

    @Override
    public boolean removeKeyframe(Keyframe keyframe) {
        keyframeGroups.getFirst().getKeyframeList().remove(keyframe);
        keyframe.removeKeyframeFromClient(player);
        return true;
    }

}
