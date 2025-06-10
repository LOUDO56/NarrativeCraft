package fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle;

import com.mojang.authlib.GameProfile;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.KeyframeControllerBase;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeCoordinate;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.screens.cameraAngles.CameraAngleControllerScreen;
import fr.loudo.narrativecraft.utils.FakePlayer;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.GameType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CameraAngleController extends KeyframeControllerBase {

    private final CameraAngleGroup cameraAngleGroup;

    public CameraAngleController(CameraAngleGroup cameraAngleGroup, ServerPlayer player, Playback.PlaybackType playbackType) {
        super(cameraAngleGroup.getCameraAngleListAsKeyframeGroup(), player, playbackType);
        this.cameraAngleGroup = cameraAngleGroup;
    }

    public void startSession() {

        cameraAngleGroup.spawnCharacters(playbackType);
        if(playbackType == Playback.PlaybackType.DEVELOPMENT) {
            NarrativeCraftMod.getInstance().getCharacterManager().reloadSkins();
            if(!cameraAngleGroup.getCameraAngleList().isEmpty()) {
                KeyframeCoordinate keyframeCoordinate = cameraAngleGroup.getCameraAngleList().getFirst().getKeyframeCoordinate();
                LocalPlayer localPlayer = Minecraft.getInstance().player;
                localPlayer.setPos(keyframeCoordinate.getVec3());
            }
            for(Keyframe keyframe : cameraAngleGroup.getCameraAngleList()) {
                keyframe.showKeyframeToClient(player);
                keyframesEntity.add(keyframe.getCameraEntity());
            }

            player.setGameMode(GameType.SPECTATOR);
            Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(new CameraAngleControllerScreen(this)));
            updateKeyframeEntityName();
            if(!cameraAngleGroup.getCharacterPositions().isEmpty()) {
                CameraAngleCharacterPosition characterPosition = cameraAngleGroup.getCharacterPositions().getFirst();
                LocalPlayer localPlayer = Minecraft.getInstance().player;
                localPlayer.setPos(characterPosition.getX(), characterPosition.getY(), characterPosition.getZ());
                localPlayer.setXRot(characterPosition.getXRot());
                localPlayer.setYRot(characterPosition.getYRot());
                localPlayer.setYHeadRot(characterPosition.getYRot());
            }
        }

    }

    @Override
    public void stopSession() {
        if(playbackType == Playback.PlaybackType.DEVELOPMENT) {
            for(Keyframe keyframe : keyframeGroups.getFirst().getKeyframeList()) {
                keyframe.removeKeyframeFromClient(player);
            }

            player.setGameMode(GameType.CREATIVE);
            NarrativeCraftFile.updateCameraAnglesFile(cameraAngleGroup.getScene());
        }
        for(CameraAngleCharacterPosition characterPosition : cameraAngleGroup.getCharacterPositions()) {
            if(characterPosition.getCharacter().getEntity() != null) {
                characterPosition.getCharacter().getEntity().remove(Entity.RemovalReason.KILLED);
            }
        }
        cameraAngleGroup.killCharacters();
        PlayerSession playerSession = Utils.getSessionOrNull(player);
        playerSession.setKeyframeControllerBase(null);
        StoryHandler.changePlayerCutsceneMode(player, playbackType, false);
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
        cameraAngleGroup.getCameraAngleList().add(cameraAngle);
        updateKeyframeEntityName();

    }

    public void editKeyframe(CameraAngle cameraAngle, String value) {
        cameraAngle.setName(value);
        updateKeyframeEntityName();
    }

    @Override
    public boolean removeKeyframe(Keyframe keyframe) {
        keyframeGroups.getFirst().getKeyframeList().remove(keyframe);
        cameraAngleGroup.getCameraAngleList().add((CameraAngle) keyframe);
        keyframe.removeKeyframeFromClient(player);
        return true;
    }

    public void addCharacter() {
        LivingEntity livingEntity = new FakePlayer(player.serverLevel(), new GameProfile(UUID.randomUUID(), "fakeP"));
        livingEntity.teleportTo(player.position().x, player.position().y, player.position().z);
        livingEntity.setXRot(player.getXRot());
        livingEntity.setYRot(player.getYRot());
        livingEntity.setYHeadRot(player.getYRot());
        if(livingEntity instanceof FakePlayer fakePlayer) {
            player.connection.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, fakePlayer));
        }
        player.serverLevel().addFreshEntity(livingEntity);
        CameraAngleCharacterPosition characterPosition = new CameraAngleCharacterPosition(
                livingEntity,
                new CharacterStory("default"),
                player.position(),
                player.getXRot(),
                player.getYRot(),
                new ArrayList<>()
        );
        cameraAngleGroup.getCharacterPositions().add(characterPosition);
    }

    public boolean isEntityInController(Entity entity) {
        for(CameraAngleCharacterPosition characterPosition : cameraAngleGroup.getCharacterPositions()) {
            if(characterPosition.getEntity().getUUID().equals(entity.getUUID())) {
                return true;
            }
        }
        return false;
    }

    public void removeCharacter(Entity entity) {
        CameraAngleCharacterPosition characterPosition = getCharacterPositionFromEntity(entity);
        cameraAngleGroup.getCharacterPositions().remove(characterPosition);
        entity.remove(Entity.RemovalReason.KILLED);
        NarrativeCraftMod.server.getPlayerList().broadcastAll(new ClientboundRemoveEntitiesPacket(entity.getId()));
    }

    public List<CharacterStory> getCharacters() {
        return cameraAngleGroup.getCharacterPositions().stream()
                .map(CameraAngleCharacterPosition::getCharacter)
                .filter(Objects::nonNull)
                .toList();
    }


    public CameraAngleCharacterPosition getCharacterPositionFromEntity(Entity entity) {
        for(CameraAngleCharacterPosition characterPosition : cameraAngleGroup.getCharacterPositions()) {
            if(characterPosition.getEntity().getUUID().equals(entity.getUUID())) {
                return characterPosition;
            }
        }
        return null;
    }

    public CameraAngleGroup getCameraAngleGroup() {
        return cameraAngleGroup;
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
        if(playbackType == Playback.PlaybackType.DEVELOPMENT) {
            currentPreviewKeyframe.openScreenOption(player);
            for(Keyframe keyframe : keyframeGroups.getFirst().getKeyframeList()) {
                keyframe.removeKeyframeFromClient(player);
            }
        }
        StoryHandler.changePlayerCutsceneMode(player, playbackType, true);
    }

    public void clearCurrentPreviewKeyframe() {
        for(Keyframe keyframe : keyframeGroups.getFirst().getKeyframeList()) {
            keyframe.showKeyframeToClient(player);
            updateKeyframeEntityName();
        }
        currentPreviewKeyframe = null;
        StoryHandler.changePlayerCutsceneMode(player, playbackType, false);
    }

}
