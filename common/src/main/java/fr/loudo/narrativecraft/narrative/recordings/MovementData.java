package fr.loudo.narrativecraft.narrative.recordings;

import net.minecraft.world.phys.Vec3;

public class MovementData {

    private double x, y, z;
    private float XRot;
    private float YRot;
    private float YHeadRot;
    private boolean isOnGround;

    public MovementData(double x, double y, double z, float XRot, float YRot, float YHeadRot, boolean isOnGround) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.XRot = XRot;
        this.YRot = YRot;
        this.YHeadRot = YHeadRot;
        this.isOnGround = isOnGround;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getXRot() {
        return XRot;
    }

    public float getYRot() {
        return YRot;
    }

    public float getYHeadRot() {
        return YHeadRot;
    }

    public boolean isOnGround() {
        return isOnGround;
    }

    public Vec3 getVec3() {
        return new Vec3(x, y, z);
    }
}
