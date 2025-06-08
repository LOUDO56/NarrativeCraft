package fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle;

import com.mojang.authlib.GameProfile;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.Cutscene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeGroup;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.recordings.MovementData;
import fr.loudo.narrativecraft.screens.storyManager.scenes.cameraAngles.CameraAnglesScreen;
import fr.loudo.narrativecraft.utils.FakePlayer;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CameraAngleGroup extends NarrativeEntry {

    private transient Scene scene;
    private transient List<NarrativeEntry> templateRecord;
    private List<CameraAngle> cameraAngleList;
    private List<CameraAngleCharacterPosition> characterPositions;

    public CameraAngleGroup(Scene scene, String name, String description) {
        super(name, description);
        this.scene = scene;
        cameraAngleList = new ArrayList<>();
        characterPositions = new ArrayList<>();
        templateRecord = new ArrayList<>();
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

    public List<NarrativeEntry> getTemplateRecord() {
        return templateRecord;
    }

    public void setTemplateRecord(List<NarrativeEntry> templateRecord) {
        this.templateRecord = templateRecord;
    }

    private void spawnAndPositionEntity(Animation animation, ServerLevel serverLevel) {
        MovementData movementData = animation.getActionsData().getMovementData().getLast();
        LivingEntity livingEntity = new FakePlayer(serverLevel, new GameProfile(UUID.randomUUID(), animation.getCharacter().getName()));
        if(livingEntity instanceof FakePlayer fakePlayer) {
            NarrativeCraftMod.server.getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, fakePlayer));
            SynchedEntityData entityData = fakePlayer.getEntityData();
            EntityDataAccessor<Byte> ENTITY_LAYER = new EntityDataAccessor<>(17, EntityDataSerializers.BYTE);
            entityData.set(ENTITY_LAYER, (byte) 0b01111111);
        }
        livingEntity.snapTo(movementData.getX(), movementData.getY(), movementData.getZ());
        livingEntity.setXRot(movementData.getXRot());
        livingEntity.setYRot(movementData.getYRot());
        livingEntity.setYHeadRot(movementData.getYHeadRot());
        serverLevel.addFreshEntity(livingEntity);

        CharacterStory characterStory = animation.getCharacter();
        File skinFile = NarrativeCraftFile.getSkinFile(characterStory, animation.getSkinName());
        characterStory.getCharacterSkinController().setCurrentSkin(skinFile);
        characterStory.setEntity(livingEntity);
        CameraAngleCharacterPosition characterPosition = new CameraAngleCharacterPosition(
                livingEntity,
                characterStory,
                movementData.getX(),
                movementData.getY(),
                movementData.getZ(),
                movementData.getXRot(),
                movementData.getYRot()
        );
        characterPositions.add(characterPosition);
    }

    public void killCharacters() {
        for(CameraAngleCharacterPosition characterPosition : characterPositions) {
            characterPosition.getEntity().remove(Entity.RemovalReason.KILLED);
        }
        characterPositions.clear();
    }

    public void spawnCharactersTemp() {
        killCharacters();
        ServerLevel serverLevel = NarrativeCraftMod.server.overworld();
        for (Animation animation : getAnimations()) {
            spawnAndPositionEntity(animation, serverLevel);
        }

        for (Subscene subscene : getSubscenes()) {
            for (Animation animation : subscene.getAnimationList()) {
                spawnAndPositionEntity(animation, serverLevel);
            }
        }

        for (Cutscene cutscene : getCutscenes()) {
            for (Animation animation : cutscene.getAnimationList()) {
                spawnAndPositionEntity(animation, serverLevel);
            }
            for (Subscene subscene : cutscene.getSubsceneList()) {
                for (Animation animation : subscene.getAnimationList()) {
                    spawnAndPositionEntity(animation, serverLevel);
                }
            }
        }
    }


    public boolean animationExists(Animation animation) {
        List<Animation> animationList = getAnimations();
        for(Animation animation1 : animationList) {
            if(animation1.getName().equals(animation.getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean cutsceneExists(Cutscene cutscene) {
        List<Cutscene> cutsceneList = getCutscenes();
        for(Cutscene cutscene1 : cutsceneList) {
            if(cutscene1.getName().equals(cutscene.getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean subsceneExists(Subscene subscene) {
        List<Subscene> subsceneList = getSubscenes();
        for(Subscene subscene1 : subsceneList) {
            if(subscene1.getName().equals(subscene.getName())) {
                return true;
            }
        }
        return false;
    }

    public List<Animation> getAnimations() {
        return templateRecord.stream()
                .filter(narrativeEntry -> narrativeEntry instanceof Animation)
                .map(narrativeEntry -> (Animation) narrativeEntry)
                .toList();
    }

    public List<Cutscene> getCutscenes() {
        return templateRecord.stream()
                .filter(narrativeEntry -> narrativeEntry instanceof Cutscene)
                .map(narrativeEntry -> (Cutscene) narrativeEntry)
                .toList();
    }

    public void clearAnimations() {
        List<Animation> toRemove = new ArrayList<>();
        for(NarrativeEntry narrativeEntry : templateRecord) {
            if(narrativeEntry instanceof Animation animation) {
                toRemove.add(animation);
            }
        }
        templateRecord.removeAll(toRemove);
    }

    public void clearSubscenes() {
        List<Subscene> toRemove = new ArrayList<>();
        for(NarrativeEntry narrativeEntry : templateRecord) {
            if(narrativeEntry instanceof Subscene subscene) {
                toRemove.add(subscene);
            }
        }
        templateRecord.removeAll(toRemove);
    }

    public void clearCutscenes() {
        List<Cutscene> toRemove = new ArrayList<>();
        for(NarrativeEntry narrativeEntry : templateRecord) {
            if(narrativeEntry instanceof Cutscene cutscene) {
                toRemove.add(cutscene);
            }
        }
        templateRecord.removeAll(toRemove);
    }

    public List<Subscene> getSubscenes() {
        return templateRecord.stream()
                .filter(narrativeEntry -> narrativeEntry instanceof Subscene)
                .map(narrativeEntry -> (Subscene) narrativeEntry)
                .toList();
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
            ScreenUtils.sendToast(Translation.message("global.error"), Translation.message("screen.camera_angles_manager.update.failed", name));
            return;
        }
        ScreenUtils.sendToast(Translation.message("global.info"), Translation.message("toast.description.updated"));
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
