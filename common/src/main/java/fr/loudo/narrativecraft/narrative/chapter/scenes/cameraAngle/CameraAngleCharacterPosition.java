package fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle;

import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.utils.FakePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class CameraAngleCharacterPosition {

    private transient LivingEntity entity;
    private CharacterStory character;
    private double x, y, z;
    private float XRot, YRot;

    public CameraAngleCharacterPosition(LivingEntity entity, CharacterStory character, double x, double y, double z, float XRot, float YRot) {
        this.entity = entity;
        this.character = character;
        this.x = x;
        this.y = y;
        this.z = z;
        this.XRot = XRot;
        this.YRot = YRot;
    }

    public CameraAngleCharacterPosition(LivingEntity entity, CharacterStory character, Vec3 vec3, float XRot, float YRot) {
        this.entity = entity;
        this.character = character;
        this.x = vec3.x;
        this.y = vec3.y;
        this.z = vec3.z;
        this.XRot = XRot;
        this.YRot = YRot;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(LivingEntity entity) {
        this.entity = entity;
    }

    public CharacterStory getCharacter() {
        return character;
    }

    public void setCharacter(CharacterStory character) {
        this.character = character;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getXRot() {
        return XRot;
    }

    public void setXRot(float XRot) {
        this.XRot = XRot;
    }

    public float getYRot() {
        return YRot;
    }

    public void setYRot(float YRot) {
        this.YRot = YRot;
    }
}
