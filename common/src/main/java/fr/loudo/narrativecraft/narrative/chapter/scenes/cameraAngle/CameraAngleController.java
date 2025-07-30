package fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.mixin.fields.PlayerListFields;
import fr.loudo.narrativecraft.narrative.chapter.scenes.KeyframeControllerBase;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeCoordinate;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeTrigger;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.screens.cameraAngles.CameraAngleControllerScreen;
import fr.loudo.narrativecraft.utils.FakePlayer;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class CameraAngleController extends KeyframeControllerBase {

    protected final AtomicInteger keyframeCounter = new AtomicInteger();

    protected CameraAngleGroup cameraAngleGroup;
    protected List<CameraAngle> oldCameraAngleDataList;
    protected List<CharacterStoryData> oldCharacterStoryDataList;

    public CameraAngleController(CameraAngleGroup cameraAngleGroup, ServerPlayer player, Playback.PlaybackType playbackType) {
        super(player, playbackType);
        this.cameraAngleGroup = cameraAngleGroup;
        initOldData();
    }

    public void initOldData() {
        CameraAngleGroup oldCameraAngleGroup = NarrativeCraftFile.getCameraAngleData(cameraAngleGroup);
        if(oldCameraAngleGroup == null) return;
        oldCameraAngleDataList = oldCameraAngleGroup.getCameraAngleList();
        oldCharacterStoryDataList = oldCameraAngleGroup.getCharacterStoryDataList();
        for(CharacterStoryData characterStoryData : oldCharacterStoryDataList) {
            CharacterStory originalCharacter = cameraAngleGroup.getCharacterStoryData(characterStoryData.getCharacterStory().getName()).getCharacterStory();
            characterStoryData.setCharacterStory(originalCharacter);
        }
    }

    public void startSession() {

        PlayerSession playerSession = NarrativeCraftMod.getInstance().getPlayerSession();
        KeyframeControllerBase keyframeControllerBase = playerSession.getKeyframeControllerBase();
        if(keyframeControllerBase != null) {
            keyframeControllerBase.stopSession(false);
        }
        playerSession.setKeyframeControllerBase(this);

        cameraAngleGroup.spawnCharacters(playbackType);
        if(playbackType == Playback.PlaybackType.DEVELOPMENT) {
            NarrativeCraftMod.getInstance().getCharacterManager().reloadSkins();
            if(!cameraAngleGroup.getCameraAngleList().isEmpty()) {
                KeyframeCoordinate keyframeCoordinate = cameraAngleGroup.getCameraAngleList().getFirst().getKeyframeCoordinate();
                LocalPlayer localPlayer = Minecraft.getInstance().player;
                localPlayer.setPos(keyframeCoordinate.getVec3());
                keyframeCounter.set(cameraAngleGroup.getCameraAngleList().getLast().getId());
            }
            for(CameraAngle cameraAngle : cameraAngleGroup.getCameraAngleList()) {
                cameraAngle.showKeyframeToClient(player);
            }
            for(KeyframeTrigger keyframeTrigger : cameraAngleGroup.getKeyframeTriggerList()) {
                keyframeTrigger.showKeyframeToClient(player);
            }

            Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(new CameraAngleControllerScreen(this)));
            updateKeyframeEntityName();
            if(!cameraAngleGroup.getCharacterStoryDataList().isEmpty()) {
                CharacterStoryData characterStoryData = cameraAngleGroup.getCharacterStoryDataList().getFirst();
                LocalPlayer localPlayer = Minecraft.getInstance().player;
                localPlayer.setPos(characterStoryData.getX(), characterStoryData.getY(), characterStoryData.getZ());
                localPlayer.setXRot(characterStoryData.getPitch());
                localPlayer.setYRot(characterStoryData.getYaw());
                localPlayer.setYHeadRot(characterStoryData.getYaw());
            }
        }

    }

    @Override
    public void stopSession(boolean save) {
        if(playbackType == Playback.PlaybackType.DEVELOPMENT) {
            cameraAngleGroup.killCharacters();
            for(CameraAngle cameraAngle : cameraAngleGroup.getCameraAngleList()) {
                cameraAngle.removeKeyframeFromClient(player);
            }
            for(KeyframeTrigger keyframeTrigger : cameraAngleGroup.getKeyframeTriggerList()) {
                keyframeTrigger.removeKeyframeFromClient(player);
            }
            if(save) {
                NarrativeCraftFile.updateCameraAnglesFile(cameraAngleGroup.getScene());
            } else {
                if(oldCameraAngleDataList != null) {
                    cameraAngleGroup.getCameraAngleList().clear();
                    cameraAngleGroup.getCameraAngleList().addAll(oldCameraAngleDataList);
                }
                if(oldCharacterStoryDataList != null) {
                    cameraAngleGroup.getCharacterStoryDataList().clear();
                    cameraAngleGroup.getCharacterStoryDataList().addAll(oldCharacterStoryDataList);
                }
            }
            player.setGameMode(GameType.CREATIVE);
        }
        PlayerSession playerSession = NarrativeCraftMod.getInstance().getPlayerSession();
        playerSession.setKeyframeControllerBase(null);
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
        CameraAngle cameraAngle = new CameraAngle(keyframeCounter.incrementAndGet(), keyframeCoordinate, name);
        cameraAngle.showKeyframeToClient(player);
        cameraAngleGroup.getCameraAngleList().add(cameraAngle);
        updateKeyframeEntityName();

    }

    @Override
    public CameraAngle getKeyframeByEntity(Entity entity) {
        for(CameraAngle cameraAngle : cameraAngleGroup.getCameraAngleList()) {
            if(cameraAngle.getCameraEntity().getId() == entity.getId()) {
                return cameraAngle;
            }
        }
        return null;
    }

    @Override
    public CameraAngle getNextKeyframe(Keyframe current) {
        List<CameraAngle> cameraAngles = cameraAngleGroup.getCameraAngleList();
        for (int j = 0; j < cameraAngles.size(); j++) {
            if (cameraAngles.get(j).getId() == current.getId()) {
                if (j + 1 < cameraAngles.size()) {
                    return cameraAngles.get(j + 1);
                }
            }
        }
        return null;
    }

    @Override
    public CameraAngle getPreviousKeyframe(Keyframe current) {
        List<CameraAngle> cameraAngles = cameraAngleGroup.getCameraAngleList();
        for (int j = 0; j < cameraAngles.size(); j++) {
            if (cameraAngles.get(j).getId() == current.getId()) {
                if (j > 0) {
                    return cameraAngles.get(j - 1);
                }
            }
        }
        return null;
    }

    public void editKeyframe(CameraAngle cameraAngle, String value) {
        cameraAngle.setName(value);
        updateKeyframeEntityName();
    }

    @Override
    public boolean removeKeyframe(Keyframe keyframe) {
        cameraAngleGroup.getCameraAngleList().remove((CameraAngle) keyframe);
        keyframe.removeKeyframeFromClient(player);
        return true;
    }

    @Override
    public void renderHUDInfo(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        String infoText = Translation.message("camera_angle.hud").getString();
        int width = minecraft.getWindow().getGuiScaledWidth();
        guiGraphics.drawString(
                font,
                infoText,
                width / 2 - font.width(infoText) / 2,
                10,
                ChatFormatting.WHITE.getColor()
        );
    }

    @Override
    protected void hideKeyframes() {
        for(CameraAngle cameraAngle : cameraAngleGroup.getCameraAngleList()) {
            cameraAngle.removeKeyframeFromClient(player);
        }
        for(KeyframeTrigger keyframeTrigger : cameraAngleGroup.getKeyframeTriggerList()) {
            keyframeTrigger.removeKeyframeFromClient(player);
        }
    }

    @Override
    protected void revealKeyframes() {
        for(CameraAngle cameraAngle : cameraAngleGroup.getCameraAngleList()) {
            cameraAngle.showKeyframeToClient(player);
            updateKeyframeEntityName();
        }
        for(KeyframeTrigger keyframeTrigger : cameraAngleGroup.getKeyframeTriggerList()) {
            keyframeTrigger.showKeyframeToClient(player);
        }
    }

    public boolean isEntityInController(Entity entity) {
        for(CharacterStoryData characterStoryData : cameraAngleGroup.getCharacterStoryDataList()) {
            if(characterStoryData.getCharacterStory().getEntity().getUUID().equals(entity.getUUID())) {
                return true;
            }
        }
        return false;
    }

    public CharacterStoryData getCharacterDataByEntity(Entity entity) {
        for(CharacterStoryData characterStoryData : cameraAngleGroup.getCharacterStoryDataList()) {
            if(characterStoryData.getCharacterStory().getEntity().getUUID().equals(entity.getUUID())) {
                return characterStoryData;
            }
        }
        return null;
    }

    public void removeCharacter(Entity entity) {
        CharacterStoryData characterStoryData = getCharacterPositionFromEntity(entity);
        cameraAngleGroup.getCharacterStoryDataList().remove(characterStoryData);
        entity.remove(Entity.RemovalReason.KILLED);
        if(entity instanceof FakePlayer fakePlayer) {
            ((PlayerListFields)NarrativeCraftMod.server.getPlayerList()).getPlayersByUUID().remove(fakePlayer.getUUID());
            NarrativeCraftMod.server.getPlayerList().remove(fakePlayer);
        }
        NarrativeCraftMod.server.getPlayerList().broadcastAll(new ClientboundRemoveEntitiesPacket(entity.getId()));
    }

    public List<CharacterStory> getCharacters() {
        return cameraAngleGroup.getCharacterStoryDataList().stream()
                .map(CharacterStoryData::getCharacterStory)
                .filter(Objects::nonNull)
                .toList();
    }


    public CharacterStoryData getCharacterPositionFromEntity(Entity entity) {
        for(CharacterStoryData characterStoryData : cameraAngleGroup.getCharacterStoryDataList()) {
            if(characterStoryData.getCharacterStory().getEntity().getUUID().equals(entity.getUUID())) {
                return characterStoryData;
            }
        }
        return null;
    }

    public CameraAngleGroup getCameraAngleGroup() {
        return cameraAngleGroup;
    }

    private void updateKeyframeEntityName() {
        for(CameraAngle cameraAngle : cameraAngleGroup.getCameraAngleList()) {
            cameraAngle.getCameraEntity().setCustomName(Component.literal(cameraAngle.getName()));
            cameraAngle.getCameraEntity().setCustomNameVisible(true);
            cameraAngle.updateEntityData(player);
        }
    }

    public void setCurrentPreviewKeyframe(CameraAngle currentPreviewKeyframe) {
        this.currentPreviewKeyframe = currentPreviewKeyframe;
        if(playbackType == Playback.PlaybackType.DEVELOPMENT) {
            currentPreviewKeyframe.openScreenOption(player);
            hideKeyframes();
        }
        StoryHandler.changePlayerCutsceneMode(playbackType, true);
    }

    public void clearCurrentPreviewKeyframe() {
        revealKeyframes();
        currentPreviewKeyframe = null;
        StoryHandler.changePlayerCutsceneMode(playbackType, false);
        Minecraft.getInstance().options.hideGui = false;

    }

}
