package fr.loudo.narrativecraft.narrative.story.inkAction;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.inkAction.InkActionResult;
import fr.loudo.narrativecraft.narrative.story.inkAction.enums.InkTagType;
import fr.loudo.narrativecraft.narrative.story.inkAction.validation.ErrorLine;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class ShakeScreenInkAction extends InkAction {

    private static final float PIXEL = 0.025f;
    private float noiseShakeSpeed;
    private float noiseShakeStrength;
    private float shakeDecayRate;

    private SimplexNoise noise;
    private float noiseI = 0.0f;
    private float shakeStrength = 0.0f;
    private boolean shaking = false;

    private float lastOffsetX = 0.0f;
    private float lastOffsetY = 0.0f;
    private float currentOffsetX = 0.0f;
    private float currentOffsetY = 0.0f;

    public ShakeScreenInkAction(StoryHandler storyHandler, String command) {
        super(storyHandler, InkTagType.SHAKE, command);
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

    public void shake(PoseStack poseStack, float partialTick) {
        if (!shaking) return;

        float interpolatedX = Mth.lerp(partialTick, lastOffsetX, currentOffsetX);
        float interpolatedY = Mth.lerp(partialTick, lastOffsetY, currentOffsetY);

        poseStack.translate(interpolatedX, interpolatedY, 0);
    }

    @Override
    public InkActionResult execute() {
        if(command.length < 4) return InkActionResult.error(this.getClass(), "");
        noise = new SimplexNoise(RandomSource.create());

        try {
            noiseShakeStrength = Float.parseFloat(command[1]);
        } catch (NumberFormatException e) {
            return InkActionResult.error(this.getClass(), Translation.message("validation.number", command[1]).getString());
        }

        try {
            shakeDecayRate = Float.parseFloat(command[2]);
        } catch (NumberFormatException e) {
            return InkActionResult.error(this.getClass(), Translation.message("validation.number", command[2]).getString());
        }

        try {
            noiseShakeSpeed = Float.parseFloat(command[3]);
        } catch (NumberFormatException e) {
            return InkActionResult.error(this.getClass(), Translation.message("validation.number", command[3]).getString());
        }

        shakeStrength = noiseShakeStrength * PIXEL;
        shaking = true;
        storyHandler.getInkActionList().removeIf(inkAction -> inkAction instanceof ShakeScreenInkAction);
        storyHandler.getInkActionList().add(this);
        sendDebugDetails();
        return InkActionResult.pass();
    }

    @Override
    void sendDebugDetails() {
        if(storyHandler.isDebugMode()) {
            Minecraft.getInstance().player.displayClientMessage(
                    Translation.message("debug.shake", noiseShakeStrength, shakeDecayRate, noiseShakeSpeed),
                    false
            );
        }
    }

    @Override
    public ErrorLine validate(String[] command, int line, String lineText, Scene scene) {
        if(command.length < 4) return new ErrorLine(
                line,
                scene,
                Translation.message("validation.missing_values").getString(),
                lineText,
                false
        );
        for (int i = 1; i < 4; i++) {
            try {
                Float.parseFloat(command[i]);
            } catch (NumberFormatException e) {
                return new ErrorLine(
                        line,
                        scene,
                        Translation.message("validation.number", command[i]).getString(),
                        lineText,
                        false
                );
            }
        }
        return null;
    }

    public boolean isShaking() {
        return shaking;
    }
}
