package fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle;

import com.mojang.authlib.GameProfile;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeGroup;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.recordings.actions.*;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
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
    private List<CameraAngle> cameraAngleList;
    private List<CameraAngleCharacterPosition> characterPositions;

    public CameraAngleGroup(Scene scene, String name, String description) {
        super(name, description);
        this.scene = scene;
        cameraAngleList = new ArrayList<>();
        characterPositions = new ArrayList<>();
    }

    public void spawnCharacters(Playback.PlaybackType playbackType) {
        for(CameraAngleCharacterPosition characterPosition : characterPositions) {
            CharacterStory characterStory = characterPosition.getCharacter();
            LivingEntity livingEntity = spawnCharacter(
                    characterStory,
                    characterPosition.getSkinName(),
                    characterPosition.getX(),
                    characterPosition.getY(),
                    characterPosition.getZ(),
                    characterPosition.getXRot(),
                    characterPosition.getYRot(),
                    characterPosition.getActions(),
                    playbackType
            );
            if(livingEntity != null) {
                characterStory.setEntity(livingEntity);
                characterPosition.setEntity(livingEntity);
            }
            if(playbackType == Playback.PlaybackType.PRODUCTION) {
                StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
                boolean notInWorld = storyHandler.getCurrentCharacters().stream().noneMatch(characterStory1 -> characterStory1.getName().equalsIgnoreCase(characterStory.getName()));
                if(notInWorld) {
                    storyHandler.getCurrentCharacters().add(characterStory);
                }
            }
        }
    }

    public void killCharacters() {
        for(CameraAngleCharacterPosition characterPosition : characterPositions) {
            if(characterPosition.getEntity() != null) {
                characterPosition.getEntity().remove(Entity.RemovalReason.KILLED);
            }
        }
    }

    public void addCharacter(CharacterStory characterStory, String skinName, double x, double y, double z, float XRot, float YRot, List<Action> actions, Playback.PlaybackType playbackType) {
        LivingEntity livingEntity = spawnCharacter(characterStory, skinName, x, y, z, XRot, YRot, actions, playbackType);
        CameraAngleCharacterPosition characterPosition = new CameraAngleCharacterPosition(
                livingEntity,
                characterStory,
                x,
                y,
                z,
                XRot,
                YRot,
                actions
        );
        characterPosition.getCharacter().setEntity(livingEntity);
        characterPosition.setEntity(livingEntity);
        characterPositions.add(characterPosition);
    }

    public LivingEntity spawnCharacter(CharacterStory characterStory, String skinName, double x, double y, double z, float XRot, float YRot, List<Action> actions, Playback.PlaybackType playbackType) {
        if(playbackType == Playback.PlaybackType.PRODUCTION) {
            StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
            for(CameraAngleCharacterPosition characterPosition : characterPositions) {
                for(CharacterStory characterStory1 : storyHandler.getCurrentCharacters()) {
                    if(characterPosition.getCharacter().getName().equals(characterStory1.getName())) {
                        return null;
                    }
                }
            }
        }
        ServerLevel serverLevel = NarrativeCraftMod.server.getLevel(Minecraft.getInstance().level.dimension());
        LivingEntity livingEntity = new FakePlayer(serverLevel, new GameProfile(UUID.randomUUID(), characterStory.getName()));
        if(livingEntity instanceof FakePlayer fakePlayer) {
            serverLevel.getServer().getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, fakePlayer));
            SynchedEntityData entityData = fakePlayer.getEntityData();
            EntityDataAccessor<Byte> ENTITY_LAYER = new EntityDataAccessor<>(17, EntityDataSerializers.BYTE);
            entityData.set(ENTITY_LAYER, (byte) 0b01111111);
        }
        livingEntity.snapTo(x, y, z);
        livingEntity.setXRot(XRot);
        livingEntity.setYRot(YRot);
        livingEntity.setYHeadRot(YRot);
        serverLevel.addFreshEntity(livingEntity);
        for(Action action : actions) {
            Action.parseAndExecute(action, livingEntity);
        }
        File skinFile = null;
        if(playbackType == Playback.PlaybackType.DEVELOPMENT) {
            skinFile = NarrativeCraftFile.getSkinFile(characterStory, skinName);
        } else if (playbackType == Playback.PlaybackType.PRODUCTION){
            skinFile = characterStory.getCharacterSkinController().getSkinFile(skinName);
        }
        characterStory.getCharacterSkinController().setCurrentSkin(skinFile);
        return livingEntity;
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
            if(cameraAngle.getName().equalsIgnoreCase(name)) {
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
