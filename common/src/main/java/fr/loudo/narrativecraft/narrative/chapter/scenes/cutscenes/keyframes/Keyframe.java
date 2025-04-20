package fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes;

import com.mojang.datafixers.util.Pair;
import fr.loudo.narrativecraft.items.CutsceneEditItems;
import fr.loudo.narrativecraft.mixin.fields.ArmorStandFields;
import fr.loudo.narrativecraft.screens.KeyframeOptionScreen;
import fr.loudo.narrativecraft.utils.PlayerCoord;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Rotations;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class Keyframe {

    private transient ArmorStand cameraEntity;
    private int id;
    private PlayerCoord position;
    private long startDelay;
    private long pathTime;
    private int tick;
    private int fov;
    private boolean isParentGroup;

    public Keyframe(int id, PlayerCoord position, int tick, long startDelay, long pathTime, int fov) {
        this.id = id;
        this.tick = tick;
        this.position = position;
        this.startDelay = startDelay;
        this.pathTime = pathTime;
        this.fov = fov;
        this.isParentGroup = false;
    }

    public void showKeyframeToClient(ServerPlayer player) {
        cameraEntity = new ArmorStand(EntityType.ARMOR_STAND, player.level());
        ((ArmorStandFields) cameraEntity).callSetSmall(true);
        cameraEntity.setNoGravity(true);
        cameraEntity.setInvisible(true);
        cameraEntity.setNoBasePlate(true);
        BlockPos blockPos = new BlockPos((int) position.getX(), (int) position.getY(), (int) position.getZ());
        player.connection.send(new ClientboundAddEntityPacket(cameraEntity, 0, blockPos));
        player.connection.send(new ClientboundSetEquipmentPacket(
                cameraEntity.getId(),
                List.of(new Pair<>(EquipmentSlot.HEAD, CutsceneEditItems.camera))
        ));
        updateItemData(player);
    }

    public void showStartGroupText(ServerPlayer player, int id) {
        cameraEntity.setCustomNameVisible(true);
        cameraEntity.setCustomName(Translation.message("cutscene.keyframegroup.text_display", id));
        isParentGroup = true;
        updateItemData(player);
    }

    public void removeKeyframeFromClient(ServerPlayer player) {
        player.connection.send(new ClientboundRemoveEntitiesPacket(cameraEntity.getId()));
    }

    public void updateItemData(ServerPlayer player) {
        float XheadPos = Utils.get360Angle(position.getXRot());
        cameraEntity.setHeadPose(new Rotations(XheadPos == 0 ? 0.000001f : Utils.get360Angle(position.getXRot()), 0, position.getZRot()));
        cameraEntity.setBodyPose(new Rotations(180f, 0, 0));
        cameraEntity.setLeftLegPose(new Rotations(180f, 0, 0));
        cameraEntity.setRightLegPose(new Rotations(180f, 0, 0));

        cameraEntity.setXRot(position.getXRot());
        cameraEntity.setYRot(position.getYRot());
        cameraEntity.setYHeadRot(position.getYRot());
        cameraEntity.setYBodyRot(player.yBodyRot);
        Vec3 playerCoordVec3 = new Vec3(position.getX(), position.getY() - 1, position.getZ());
        PositionMoveRotation pos = new PositionMoveRotation(
                playerCoordVec3,
                new Vec3(0, 0, 0),
                position.getYRot(),
                position.getXRot()
        );
        player.connection.send(new ClientboundEntityPositionSyncPacket(
                cameraEntity.getId(),
                pos,
                false
        ));
        player.connection.send(new ClientboundSetEntityDataPacket(cameraEntity.getId(), cameraEntity.getEntityData().getNonDefaultValues()));
    }

    public PlayerCoord getPosition() {
        return position;
    }

    public void setPosition(PlayerCoord position) {
        this.position = position;
    }

    public long getStartDelay() {
        return startDelay;
    }


    public void setStartDelay(long startDelay) {
        this.startDelay = startDelay;
    }

    public long getPathTime() {
        return pathTime;
    }

    public void setPathTime(long pathTime) {
        this.pathTime = pathTime;
    }

    public ArmorStand getCameraEntity() {
        return cameraEntity;
    }

    public int getId() {
        return id;
    }

    public int getFov() {
        return fov;
    }

    public void setFov(int fov) {
        this.fov = fov;
    }

    public void openScreenOption(ServerPlayer player) {
        Minecraft client = Minecraft.getInstance();
        KeyframeOptionScreen screen = new KeyframeOptionScreen(this, player);
        client.execute(() -> client.setScreen(screen));
    }

    public int getTick() {
        return tick;
    }

    public void setTick(int tick) {
        this.tick = tick;
    }

    public boolean isParentGroup() {
        return isParentGroup;
    }
}
