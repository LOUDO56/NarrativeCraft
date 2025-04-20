package fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes;

import net.minecraft.world.phys.Vec3;

public class KeyframeCoordinate {

    private double x, y, z;
    private float XRot, YRot, ZRot, fov;

    public KeyframeCoordinate(double x, double y, double z, float XRot, float YRot, float fov) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.XRot = XRot;
        this.YRot = YRot;
        this.ZRot = 0f;
        this.fov = fov;
    }

    public KeyframeCoordinate(Vec3 position, float XRot, float YRot, float fov) {
        this.x = position.x();
        this.y = position.y();
        this.z = position.z();
        this.XRot = XRot;
        this.YRot = YRot;
        this.ZRot = 0f;
        this.fov = fov;
    }

    public Vec3 getVec3() {
        return new Vec3(x, y, z);
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

    public float getZRot() {
        return ZRot;
    }

    public void setZRot(float ZRot) {
        this.ZRot = ZRot;
    }

    public float getFov() {
        return fov;
    }

    public void setFov(float fov) {
        this.fov = fov;
    }
}
