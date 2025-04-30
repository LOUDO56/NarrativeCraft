package fr.loudo.narrativecraft.narrative.dialog;

import fr.loudo.narrativecraft.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.phys.Vec3;

public class DialogInterpolation {

    private Vec3 startPosition, endPosition, textPosition;
    private float endScale, scale;
    private float opacity;

    public DialogInterpolation(Vec3 textPosition, float scale, float opacity) {
        this.textPosition = textPosition;
        this.scale = scale;
        this.opacity = opacity;
    }

    public DialogInterpolation(Dialog dialog, Vec3 startPosition, Vec3 endPosition, float scale) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.textPosition = startPosition;
        this.endScale = scale;
        this.scale = 0;
        this.opacity = 0;
    }

    public DialogInterpolation getNextValues(double t) {
        double x = MathUtils.lerp(startPosition.x, endPosition.x, t);
        double y = MathUtils.lerp(startPosition.y, endPosition.y, t);
        double z = MathUtils.lerp(startPosition.z, endPosition.z, t);
        float scale = (float) MathUtils.lerp(0, endScale, t);
        float opacity = (float) MathUtils.lerp(0, 100, t);
        return new DialogInterpolation(new Vec3(x, y, z), scale, opacity);
    }

    public Vec3 getTextPosition() {
        return textPosition;
    }

    public float getScale() {
        return scale;
    }

    public float getOpacity() {
        return opacity;
    }

    public void setStartPosition(Vec3 startPosition) {
        this.startPosition = startPosition;
    }
}
