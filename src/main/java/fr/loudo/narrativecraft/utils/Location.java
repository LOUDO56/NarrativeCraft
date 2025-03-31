package fr.loudo.narrativecraft.utils;

public class Location {

    private double x, y, z;
    private float XRot;
    private float YRot;
    private float YHeadRot;

    public Location(double x, double y, double z, float XRot, float YRot, float YHeadRot) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.XRot = XRot;
        this.YRot = YRot;
        this.YHeadRot = YHeadRot;
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
}
