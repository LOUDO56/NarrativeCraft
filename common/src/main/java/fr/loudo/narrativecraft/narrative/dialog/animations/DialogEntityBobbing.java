package fr.loudo.narrativecraft.narrative.dialog.animations;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.dialog.Dialog;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class DialogEntityBobbing {

    private final Dialog dialog;

    private static final float PIXEL = 0.025f;
    private float noiseShakeSpeed;
    private float noiseShakeStrength;
    private float shakeDecayRate;

    private SimplexNoise noise;
    private float noiseI = 0.0f;
    private float shakeStrength;

    private float lastOffsetX = 0.0f;
    private float lastOffsetY = 0.0f;
    private float currentOffsetX = 0.0f;
    private float currentOffsetY = 0.0f;

    private float lastXRot;
    private float lastYRot;

    public DialogEntityBobbing(Dialog dialog, float noiseShakeSpeed, float noiseShakeStrength) {
        this.dialog = dialog;
        lastXRot = dialog.getEntityServer().getXRot();
        lastYRot = dialog.getEntityServer().getYRot();
        noise = new SimplexNoise(RandomSource.create());
        this.noiseShakeSpeed = noiseShakeSpeed;
        this.shakeDecayRate = 0;
        this.noiseShakeStrength = noiseShakeStrength;
        shakeStrength = noiseShakeStrength * PIXEL;
    }

    public void tick() {
        if (Minecraft.getInstance().isPaused()) return;

        noiseI += (1.0f / 20.0f) * noiseShakeSpeed;

        shakeStrength = Mth.lerp(shakeDecayRate * (1.0f / 20.0f), shakeStrength, 0.0f);

        lastOffsetX = currentOffsetX;
        lastOffsetY = currentOffsetY;

        currentOffsetX = (float) noise.getValue(1, noiseI) * shakeStrength;
        currentOffsetY = (float) noise.getValue(100, noiseI) * shakeStrength;

        if (Math.abs(shakeStrength) <= 0) {
            currentOffsetX = currentOffsetY = lastOffsetX = lastOffsetY = 0;
        }
    }

    public void updateLookDirection(float partialTick) {

        for(Playback playback : NarrativeCraftMod.getInstance().getPlaybackHandler().getPlaybacks()) {
            if(playback.getCharacter().getName().equals(dialog.getCharacterName())) {
                if(playback.isPlaying()) {
                    lastXRot = dialog.getEntityClient().getXRot();
                    lastYRot = dialog.getEntityClient().getYRot();
                    return;
                }
            }
        }

        float interpolatedX = Mth.lerp(partialTick, lastOffsetX, currentOffsetX);
        float interpolatedY = Mth.lerp(partialTick, lastOffsetY, currentOffsetY);

        dialog.getEntityServer().setYRot(lastYRot + interpolatedY);
        dialog.getEntityServer().setYHeadRot(lastYRot + interpolatedY);
        dialog.getEntityServer().setXRot(lastXRot + interpolatedX);

    }

    public float getNoiseShakeStrength() {
        return noiseShakeStrength;
    }

    public void setNoiseShakeStrength(float noiseShakeStrength) {
        this.noiseShakeStrength = noiseShakeStrength;
        shakeStrength = noiseShakeStrength * PIXEL;
    }

    public float getNoiseShakeSpeed() {
        return noiseShakeSpeed;
    }

    public void setNoiseShakeSpeed(float noiseShakeSpeed) {
        this.noiseShakeSpeed = noiseShakeSpeed;
    }
}
