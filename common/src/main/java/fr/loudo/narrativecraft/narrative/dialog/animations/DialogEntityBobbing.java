package fr.loudo.narrativecraft.narrative.dialog.animations;

import fr.loudo.narrativecraft.narrative.dialog.Dialog;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class DialogEntityBobbing {

    private final Dialog dialog;

    private static final float PIXEL = 0.025f;
    private float noiseShakeSpeed = 100;
    private float noiseShakeStrength = 250;
    private float shakeDecayRate = 0;

    private SimplexNoise noise;
    private float noiseI = 0.0f;
    private float shakeStrength;
    private boolean shaking;

    private float lastOffsetX = 0.0f;
    private float lastOffsetY = 0.0f;
    private float currentOffsetX = 0.0f;
    private float currentOffsetY = 0.0f;

    private float lastXRot;
    private float lastYRot;

    public DialogEntityBobbing(Dialog dialog) {
        this.dialog = dialog;
        lastXRot = dialog.getEntityServer().getXRot();
        lastYRot = dialog.getEntityServer().getYRot();
        noise = new SimplexNoise(RandomSource.create());
        shakeStrength = noiseShakeStrength * PIXEL;
        shaking = true;
    }

    public void tick() {
        if (!shaking || Minecraft.getInstance().isPaused()) return;

        noiseI += (1.0f / 20.0f) * noiseShakeSpeed;

        shakeStrength = Mth.lerp(shakeDecayRate * (1.0f / 20.0f), shakeStrength, 0.0f);

        lastOffsetX = currentOffsetX;
        lastOffsetY = currentOffsetY;

        currentOffsetX = (float) noise.getValue(1, noiseI) * shakeStrength;
        currentOffsetY = (float) noise.getValue(100, noiseI) * shakeStrength;

        if (Math.abs(shakeStrength) <= 0) {
            shaking = false;
            currentOffsetX = currentOffsetY = lastOffsetX = lastOffsetY = 0;
        }
    }

    public void updateLookDirection(float partialTick) {
        if (!shaking) return;

        float interpolatedX = Mth.lerp(partialTick, lastOffsetX, currentOffsetX);
        float interpolatedY = Mth.lerp(partialTick, lastOffsetY, currentOffsetY);

        dialog.getEntityServer().setYRot(lastYRot + interpolatedY);
        dialog.getEntityServer().setYHeadRot(lastYRot + interpolatedY);
        dialog.getEntityServer().setXRot(lastXRot + interpolatedX);

    }


}
