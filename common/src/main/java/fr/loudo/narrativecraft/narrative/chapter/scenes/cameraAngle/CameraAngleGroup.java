package fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle;

import com.mojang.datafixers.util.Pair;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeGroup;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.screens.storyManager.scenes.cameraAngles.CameraAnglesScreen;
import fr.loudo.narrativecraft.utils.FakePlayer;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.ArrayList;
import java.util.List;

public class CameraAngleGroup extends NarrativeEntry {

    private transient Scene scene;
    private List<CameraAngle> cameraAngleList;
    private final List<CharacterStoryData> characterStoryDataList;

    public CameraAngleGroup(Scene scene, String name, String description) {
        super(name, description);
        this.scene = scene;
        cameraAngleList = new ArrayList<>();
        characterStoryDataList = new ArrayList<>();
    }

    public void spawnCharacters(Playback.PlaybackType playbackType) {
        for(CharacterStoryData characterStoryData : characterStoryDataList) {
            CharacterStory characterStory = characterStoryData.getCharacterStory();
            spawnCharacter(characterStoryData, playbackType);
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
        for(CharacterStoryData characterStoryData : characterStoryDataList) {
            if(characterStoryData.getCharacterStory().getEntity() != null) {
                characterStoryData.getCharacterStory().getEntity().remove(Entity.RemovalReason.KILLED);
            }
        }
    }

    public CharacterStoryData addCharacter(CharacterStory characterStory, String skinName, double x, double y, double z, float XRot, float YRot, Playback.PlaybackType playbackType) {
        CharacterStoryData characterStoryData = new CharacterStoryData(
                characterStory,
                skinName,
                x,
                y,
                z,
                XRot,
                YRot
        );
        spawnCharacter(characterStoryData, playbackType);
        characterStoryDataList.add(characterStoryData);
        return characterStoryData;
    }

    public void spawnCharacter(CharacterStoryData characterStoryData, Playback.PlaybackType playbackType) {
        if(playbackType == Playback.PlaybackType.PRODUCTION) {
            StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
            for(CharacterStory characterStory1 : storyHandler.getCurrentCharacters()) {
                if(characterStoryData.getCharacterStory().getName().equals(characterStory1.getName())) {
                    if(!characterStoryData.getSkinName().equals(characterStory1.getCharacterSkinController().getCurrentSkin().getName())) {
                        characterStory1.getCharacterSkinController().setCurrentSkin(characterStory1.getCharacterSkinController().getSkinFile(characterStoryData.getSkinName()));
                    }
                    if(characterStory1.getEntity() instanceof FakePlayer fakePlayer) {
                        fakePlayer.getInventory().clearContent();
                        for(CharacterStoryData.ItemSlotData itemSlotData : characterStoryData.getItemSlotDataList()) {
                            fakePlayer.getServer().getPlayerList().broadcastAll(new ClientboundSetEquipmentPacket(
                                    fakePlayer.getId(),
                                    List.of(new Pair<>(EquipmentSlot.valueOf(itemSlotData.equipmentSlot()), itemSlotData.getItem(characterStory1.getEntity().registryAccess())))
                            ));
                        }
                        fakePlayer.setPose(characterStoryData.getPose());
                        EntityDataAccessor<Byte> entityFlagByte = new EntityDataAccessor<>(0, EntityDataSerializers.BYTE);
                        fakePlayer.getEntityData().set(entityFlagByte, characterStoryData.getEntityByte());
                    }
                    return;
                }
            }
        }
        ServerLevel serverLevel = NarrativeCraftMod.server.getLevel(Minecraft.getInstance().level.dimension());
        characterStoryData.spawn(serverLevel);
    }

    public CharacterStoryData getCharacterStoryData(String name) {
        for(CharacterStoryData characterStoryData : characterStoryDataList) {
            if(characterStoryData.getCharacterStory().getName().equalsIgnoreCase(name)) {
                return characterStoryData;
            }
        }
        return null;
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

    public List<CharacterStoryData> getCharacterStoryDataList() {
        return characterStoryDataList;
    }

    public void setCameraAngleList(List<CameraAngle> cameraAngleList) {
        this.cameraAngleList = cameraAngleList;
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
