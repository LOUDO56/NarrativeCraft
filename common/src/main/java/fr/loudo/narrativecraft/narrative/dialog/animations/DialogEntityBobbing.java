package fr.loudo.narrativecraft.narrative.dialog.animations;

import fr.loudo.narrativecraft.narrative.dialog.Dialog;

public class DialogEntityBobbing {

    private final Dialog dialog;
    private final long startTime;
    private final float baseYaw;

    public DialogEntityBobbing(Dialog dialog) {
        this.dialog = dialog;
        this.startTime = System.currentTimeMillis();
        baseYaw = dialog.getEntityServer().getYRot();
    }


    public void updateLookDirection() {
        long t = System.currentTimeMillis() - startTime;
        double time = t / 1000.0;

        double speed = 20.0;
        double pitchAmplitude = 10.0;
        double yawAmplitude = 10.0;

        float pitch = (float)(Math.sin(time * speed) * pitchAmplitude);
        float yaw = (float)(Math.cos(time * speed) * yawAmplitude);

        dialog.getEntityServer().setXRot(pitch);
        dialog.getEntityServer().setYHeadRot(baseYaw + yaw);
        dialog.getEntityServer().setYRot(baseYaw + yaw);
    }

}
