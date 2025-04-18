package fr.loudo.narrativecraft.utils;

import net.minecraft.world.phys.Vec3;

public class PlayerCoord {

    private double x, y, z;
    private float XRot, YRot;

    public PlayerCoord(double x, double y, double z, float XRot, float YRot) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.XRot = XRot;
        this.YRot = YRot;
    }

    public PlayerCoord(Vec3 position, float XRot, float YRot) {
        this.x = position.x();
        this.y = position.y();
        this.z = position.z();
        this.XRot = XRot;
        this.YRot = YRot;
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
}
