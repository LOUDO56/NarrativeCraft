package fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle;

import com.mojang.authlib.GameProfile;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.KeyframeControllerBase;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeCoordinate;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
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

    public CameraAngleController(CameraAngleGroup cameraAngleGroup, ServerPlayer player) {
        super(cameraAngleGroup.getCameraAngleListAsKeyframeGroup(), player);
        this.cameraAngleGroup = cameraAngleGroup;
    }

    public void startSession() {

        if(!cameraAngleGroup.getCameraAngleList().isEmpty()) {
            TpUtil.teleportPlayer(player, cameraAngleGroup.getCameraAngleList().getFirst().getKeyframeCoordinate().getVec3());
        }
        for(Keyframe keyframe : cameraAngleGroup.getCameraAngleList()) {
            keyframe.showKeyframeToClient(player);
            keyframesEntity.add(keyframe.getCameraEntity());
        }

        for(CameraAngleCharacterPosition characterPosition : cameraAngleGroup.getCharacterPositions()) {
            FakePlayer fakePlayer = new FakePlayer(player.serverLevel(), new GameProfile(UUID.randomUUID(), "fakeP"));
            fakePlayer.teleportTo(characterPosition.getX(), characterPosition.getY(), characterPosition.getZ());
            fakePlayer.setXRot(characterPosition.getXRot());
            fakePlayer.setYRot(characterPosition.getYRot());
            fakePlayer.setYHeadRot(characterPosition.getYRot());
            player.connection.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, fakePlayer));
            player.serverLevel().addFreshEntity(fakePlayer);
            characterPosition.setEntity(fakePlayer);
        }

        player.setGameMode(GameType.SPECTATOR);
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(new CameraAngleControllerScreen(this)));
        updateKeyframeEntityName();
    }

    @Override
    public void stopSession() {
        for(Keyframe keyframe : keyframeGroups.getFirst().getKeyframeList()) {
            keyframe.removeKeyframeFromClient(player);
        }

        for(CameraAngleCharacterPosition characterPosition : cameraAngleGroup.getCharacterPositions()) {
            characterPosition.getEntity().remove(Entity.RemovalReason.KILLED);
            player.connection.send(new ClientboundRemoveEntitiesPacket(characterPosition.getEntity().getId()));
        }

        player.setGameMode(GameType.CREATIVE);
        PlayerSession playerSession = Utils.getSessionOrNull(player);
        playerSession.setKeyframeControllerBase(null);
        NarrativeCraftFile.updateCameraAnglesFile(cameraAngleGroup.getScene());
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

}
