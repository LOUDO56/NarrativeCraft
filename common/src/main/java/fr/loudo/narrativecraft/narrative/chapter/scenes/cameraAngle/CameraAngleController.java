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
import fr.loudo.narrativecraft.utils.TpUtil;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;

import java.util.UUID;

public class CameraAngleController extends KeyframeControllerBase {

    private final CameraAngleGroup cameraAngleGroup;

    public CameraAngleController(CameraAngleGroup cameraAngleGroup, ServerPlayer player, Playback.PlaybackType playbackType) {
        super(cameraAngleGroup.getCameraAngleListAsKeyframeGroup(), player, playbackType);
        this.cameraAngleGroup = cameraAngleGroup;
    }

    public void startSession() {

        if(playbackType == Playback.PlaybackType.DEVELOPMENT) {
            if(!cameraAngleGroup.getCameraAngleList().isEmpty()) {
                TpUtil.teleportPlayer(player, cameraAngleGroup.getCameraAngleList().getFirst().getKeyframeCoordinate().getVec3());
            }
            for(Keyframe keyframe : cameraAngleGroup.getCameraAngleList()) {
                keyframe.showKeyframeToClient(player);
                keyframesEntity.add(keyframe.getCameraEntity());
            }

            player.setGameMode(GameType.SPECTATOR);
            Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(new CameraAngleControllerScreen(this)));
            updateKeyframeEntityName();
        }

        for(CameraAngleCharacterPosition characterPosition : cameraAngleGroup.getCharacterPositions()) {
            FakePlayer fakePlayer = new FakePlayer(player.serverLevel(), new GameProfile(UUID.randomUUID(), characterPosition.getCharacter().getName()));
            fakePlayer.teleportTo(characterPosition.getX(), characterPosition.getY(), characterPosition.getZ());
            fakePlayer.setXRot(characterPosition.getXRot());
            fakePlayer.setYRot(characterPosition.getYRot());
            fakePlayer.setYHeadRot(characterPosition.getYRot());
            player.connection.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, fakePlayer));
            player.serverLevel().addFreshEntity(fakePlayer);
            characterPosition.setEntity(fakePlayer);
            characterPosition.getCharacter().setEntity(fakePlayer);
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
            characterPosition.getEntity().remove(Entity.RemovalReason.KILLED);
            player.connection.send(new ClientboundRemoveEntitiesPacket(characterPosition.getEntity().getId()));
        }
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
        FakePlayer fakePlayer = new FakePlayer(player.serverLevel(), new GameProfile(UUID.randomUUID(), "fakeP"));
        fakePlayer.teleportTo(player.position().x, player.position().y, player.position().z);
        fakePlayer.setXRot(player.getXRot());
        fakePlayer.setYRot(player.getYRot());
        fakePlayer.setYHeadRot(player.getYRot());
        player.connection.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, fakePlayer));
        player.serverLevel().addFreshEntity(fakePlayer);
        CameraAngleCharacterPosition characterPosition = new CameraAngleCharacterPosition(
                fakePlayer,
                new CharacterStory("default"),
                player.position(),
                player.getXRot(),
                player.getYRot()
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
