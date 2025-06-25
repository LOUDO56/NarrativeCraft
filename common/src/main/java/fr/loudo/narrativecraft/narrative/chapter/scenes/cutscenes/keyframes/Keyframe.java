package fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes;

import com.mojang.datafixers.util.Pair;
import fr.loudo.narrativecraft.items.CutsceneEditItems;
import fr.loudo.narrativecraft.mixin.fields.ArmorStandFields;
import fr.loudo.narrativecraft.screens.cameraAngles.CameraAngleOptionsScreen;
import fr.loudo.narrativecraft.screens.cutscenes.KeyframeCutsceneOptionScreen;
import fr.loudo.narrativecraft.screens.keyframes.KeyframeOptionScreen;
import fr.loudo.narrativecraft.utils.Easing;
import fr.loudo.narrativecraft.utils.MathUtils;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Rotations;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class Keyframe {

    protected transient ArmorStand cameraEntity;
    protected int id, tick;
    protected double speed;
    protected KeyframeCoordinate keyframeCoordinate;
    protected long startDelay, pathTime, transitionDelay;
    protected boolean isParentGroup, isFixed;
    protected Easing easing;

    public Keyframe(int id, KeyframeCoordinate keyframeCoordinate, int tick, long startDelay, long pathTime) {
        this.id = id;
        this.tick = tick;
        this.keyframeCoordinate = keyframeCoordinate;
        this.startDelay = startDelay;
        this.pathTime = pathTime;
        this.transitionDelay = 0;
        this.isParentGroup = false;
        this.speed = 1;
        this.easing = Easing.SMOOTH;
        this.isFixed = false;
    }

    public Keyframe(int id, KeyframeCoordinate keyframeCoordinate) {
        this.id = id;
        this.keyframeCoordinate = keyframeCoordinate;
        this.isParentGroup = false;
        this.isFixed = true;
    }

    public void showKeyframeToClient(ServerPlayer player) {
        cameraEntity = new ArmorStand(EntityType.ARMOR_STAND, player.level());
        ((ArmorStandFields) cameraEntity).callSetSmall(true);
        cameraEntity.setNoGravity(true);
        cameraEntity.setInvisible(true);
        cameraEntity.setNoBasePlate(true);
        cameraEntity.setBodyPose(new Rotations(180f, 0, 0));
        cameraEntity.setLeftLegPose(new Rotations(180f, 0, 0));
        cameraEntity.setRightLegPose(new Rotations(180f, 0, 0));
        BlockPos blockPos = new BlockPos((int) keyframeCoordinate.getX(), (int) keyframeCoordinate.getY(), (int) keyframeCoordinate.getZ());
        player.connection.send(new ClientboundAddEntityPacket(cameraEntity, 0, blockPos));
        player.connection.send(new ClientboundSetEquipmentPacket(
                cameraEntity.getId(),
                List.of(new Pair<>(EquipmentSlot.HEAD, CutsceneEditItems.camera))
        ));
        updateEntityData(player);
    }

    public void showStartGroupText(ServerPlayer player, int id) {
        cameraEntity.setCustomNameVisible(true);
        cameraEntity.setCustomName(Translation.message("cutscene.keyframegroup.text_display", id));
        isParentGroup = true;
        updateEntityData(player);
    }

    public void removeKeyframeFromClient(ServerPlayer player) {
        player.connection.send(new ClientboundRemoveEntitiesPacket(cameraEntity.getId()));
    }

    public void updateEntityData(ServerPlayer player) {
        float XheadPos = MathUtils.get360Angle(keyframeCoordinate.getXRot());
        cameraEntity.setHeadPose(new Rotations(XheadPos == 0 ? 0.000001f : MathUtils.get360Angle(keyframeCoordinate.getXRot()), 0, keyframeCoordinate.getZRot()));

        cameraEntity.setXRot(keyframeCoordinate.getXRot());
        cameraEntity.setYRot(keyframeCoordinate.getYRot());
        cameraEntity.setYHeadRot(keyframeCoordinate.getYRot());
        cameraEntity.setYBodyRot(player.yBodyRot);
        Vec3 playerCoordVec3 = new Vec3(keyframeCoordinate.getX(), keyframeCoordinate.getY() - 1, keyframeCoordinate.getZ());
        PositionMoveRotation pos = new PositionMoveRotation(
                playerCoordVec3,
                new Vec3(0, 0, 0),
                keyframeCoordinate.getYRot(),
                keyframeCoordinate.getXRot()
        );
        player.connection.send(new ClientboundEntityPositionSyncPacket(
                cameraEntity.getId(),
                pos,
                false
        ));
        player.connection.send(new ClientboundSetEntityDataPacket(cameraEntity.getId(), cameraEntity.getEntityData().getNonDefaultValues()));
    }

    public KeyframeCoordinate getKeyframeCoordinate() {
        return keyframeCoordinate;
    }

    public void setKeyframeCoordinate(KeyframeCoordinate keyframeCoordinate) {
        this.keyframeCoordinate = keyframeCoordinate;
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

    public long getTransitionDelay() {
        return transitionDelay;
    }

    public void setTransitionDelay(long transitionDelay) {
        this.transitionDelay = transitionDelay;
    }

    public ArmorStand getCameraEntity() {
        return cameraEntity;
    }

    public int getId() {
        return id;
    }

    public void openScreenOption(ServerPlayer player) {
        Minecraft client = Minecraft.getInstance();
        KeyframeOptionScreen screen;
        if(isFixed) {
            screen = new CameraAngleOptionsScreen(this, player, false);
        } else {
            screen = new KeyframeCutsceneOptionScreen(this, player, false);
        }
        client.execute(() -> client.setScreen(screen));
    }

    public int getTick() {
        return tick;
    }

    public void setTick(int tick) {
        this.tick = tick;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public boolean isParentGroup() {
        return isParentGroup;
    }

    public Easing getEasing() {
        return easing;
    }

    public void setEasing(Easing easing) {
        this.easing = easing;
    }

    public boolean isFixed() {
        return isFixed;
    }

    public void setFixed(boolean fixed) {
        isFixed = fixed;
    }
}
