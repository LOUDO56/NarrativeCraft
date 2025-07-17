package fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle;

import com.mojang.datafixers.util.Pair;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.mixin.fields.EntityFields;
import fr.loudo.narrativecraft.mixin.fields.PlayerListFields;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeTrigger;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class CameraAngleGroup extends NarrativeEntry {

    private transient Scene scene;
    private List<CameraAngle> cameraAngleList;
    private List<KeyframeTrigger> keyframeTriggerList;
    private final List<CharacterStoryData> characterStoryDataList;

    public CameraAngleGroup(Scene scene, String name, String description) {
        super(name, description);
        this.scene = scene;
        cameraAngleList = new ArrayList<>();
        keyframeTriggerList = new ArrayList<>();
        characterStoryDataList = new ArrayList<>();
    }

    public void spawnCharacters(Playback.PlaybackType playbackType) {
        for(CharacterStoryData characterStoryData : characterStoryDataList) {
            if(characterStoryData.isOnlyTemplate() && playbackType == Playback.PlaybackType.PRODUCTION) continue;
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
                LivingEntity entity = characterStoryData.getCharacterStory().getEntity();
                characterStoryData.getCharacterStory().getEntity().remove(Entity.RemovalReason.KILLED);
                if(entity instanceof FakePlayer fakePlayer) {
                    NarrativeCraftMod.server.getPlayerList().remove(fakePlayer);
                    ((PlayerListFields)NarrativeCraftMod.server.getPlayerList()).getPlayersByUUID().remove(fakePlayer.getUUID());
                }
                StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
                if(storyHandler != null) {
                    storyHandler.getCurrentCharacters().remove(characterStoryData.getCharacterStory());
                }
            }
        }
    }

    public CharacterStoryData addCharacter(CharacterStory characterStory, String skinName, double x, double y, double z, float XRot, float YRot, Playback.PlaybackType playbackType, boolean onlyTemplate) {
        CharacterStoryData characterStoryData = new CharacterStoryData(
                characterStory,
                skinName,
                x,
                y,
                z,
                XRot,
                YRot,
                onlyTemplate
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
                            fakePlayer.setItemSlot(EquipmentSlot.valueOf(itemSlotData.equipmentSlot()), itemSlotData.getItem(characterStory1.getEntity().registryAccess()));
                        }
                        fakePlayer.setPose(characterStoryData.getPose());
                        fakePlayer.getEntityData().set(EntityFields.getDATA_SHARED_FLAGS_ID(), characterStoryData.getEntityByte());
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

    public List<KeyframeTrigger> getKeyframeTriggerList() {
        return keyframeTriggerList;
    }

    public KeyframeTrigger getKeyframeTriggerByEntity(Entity entity) {
        for(KeyframeTrigger keyframe : keyframeTriggerList) {
            if(keyframe.getCameraEntity().getId() == entity.getId()) {
                return keyframe;
            }
        }
        return null;
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
