package fr.loudo.narrativecraft.narrative.dialog.animations;

import fr.loudo.narrativecraft.narrative.dialog.Dialog;
import net.minecraft.client.Minecraft;

//TODO: fix boobing unsync when game minimized
public class DialogEntityBobbing {

    private final Dialog dialog;
    private final long startTime;
    private boolean isMinimized;
    private float lastYRot;
    private float lastXRot;

    public DialogEntityBobbing(Dialog dialog) {
        this.dialog = dialog;
        this.startTime = System.currentTimeMillis();
        isMinimized = false;
        lastXRot = 0;
        lastYRot = 0;
    }

    public void updateLookDirection() {
        long now = System.currentTimeMillis();
        Minecraft minecraft = Minecraft.getInstance();
        if(minecraft.getWindow().isMinimized() && !isMinimized) {
            isMinimized = true;
            lastYRot = dialog.getEntityServer().getYRot();
            lastXRot = dialog.getEntityServer().getXRot();
        } else if(!minecraft.getWindow().isMinimized() && isMinimized) {
            dialog.getEntityServer().setYHeadRot(lastYRot);
            dialog.getEntityServer().setYRot(lastYRot);
            dialog.getEntityServer().setXRot(lastXRot);
            isMinimized = false;
        }
        long t = now - startTime;
        double time = t / 1000.0;

        double speed = 25.0;
        double pitchAmplitude = 1.5;
        double yawAmplitude = 1.5;

        float pitchOffset = (float)(Math.sin(time * speed) * pitchAmplitude);
        float yawOffset = (float)(Math.cos(time * speed) * yawAmplitude);

        float currentPitch = dialog.getEntityServer().getXRot();
        float currentYaw = dialog.getEntityServer().getYRot();

        dialog.getEntityServer().setYHeadRot(currentYaw + yawOffset);
        dialog.getEntityServer().setYRot(currentYaw + yawOffset);
        dialog.getEntityServer().setXRot(currentPitch + pitchOffset);
    }


}
