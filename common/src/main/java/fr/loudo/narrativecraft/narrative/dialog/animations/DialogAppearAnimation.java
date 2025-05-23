package fr.loudo.narrativecraft.narrative.dialog.animations;

import fr.loudo.narrativecraft.utils.MathUtils;
import net.minecraft.world.phys.Vec3;

public class DialogAppearAnimation {

    private Vec3 startPosition, endPosition, textPosition;
    private float endScale, scale;
    private int opacity;

    public DialogAppearAnimation(Vec3 textPosition, float scale, int opacity) {
        this.textPosition = textPosition;
        this.scale = scale;
        this.opacity = opacity;
    }

    public DialogAppearAnimation(Vec3 startPosition, Vec3 endPosition, float scale) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.textPosition = startPosition;
        this.endScale = scale;
        this.scale = 0;
        this.opacity = 0;
    }

    public DialogAppearAnimation getAppearNextValue(double t) {
        double x = MathUtils.lerp(startPosition.x, endPosition.x, t);
        double y = MathUtils.lerp(startPosition.y, endPosition.y, t);
        double z = MathUtils.lerp(startPosition.z, endPosition.z, t);
        float scale = (float) MathUtils.lerp(0, endScale, t);
        int opacity = (int) MathUtils.lerp(0, 255, t);
        return new DialogAppearAnimation(new Vec3(x, y, z), scale, opacity);
    }

    public DialogAppearAnimation getDisappearNextValue(double t) {
        double x = MathUtils.lerp(endPosition.x, startPosition.x, t);
        double y = MathUtils.lerp(endPosition.y, startPosition.y, t);
        double z = MathUtils.lerp(endPosition.z, startPosition.z, t);
        float scale = (float) MathUtils.lerp(endScale, 0, t);
        int opacity = (int) MathUtils.lerp(255, 0, t);
        return new DialogAppearAnimation(new Vec3(x, y, z), scale, opacity);
    }

    public Vec3 getTextPosition() {
        return textPosition;
    }

    public float getScale() {
        return scale;
    }

    public int getOpacity() {
        return opacity;
    }

    public void setStartPosition(Vec3 startPosition) {
        this.startPosition = startPosition;
    }

    public void setEndPosition(Vec3 endPosition) {
        this.endPosition = endPosition;
    }

    public void setScale(float scale) {
        this.endScale = scale;
    }
}
