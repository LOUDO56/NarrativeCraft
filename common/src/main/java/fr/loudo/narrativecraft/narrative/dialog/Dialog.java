package fr.loudo.narrativecraft.narrative.dialog;

import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.loudo.narrativecraft.mixin.fields.GameRendererFields;
import fr.loudo.narrativecraft.utils.Easing;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

public class Dialog {

    private final long APPEAR_TIME = 200L;
    private final Easing easing = Easing.SMOOTH;

    private DialogScrollText dialogScrollText;
    private Vector4f posClip;
    private float fov, paddingX, paddingY, scale;
    private int opacity;

    private Entity entityServer;
    private Entity entityClient;
    private Vec3 textPosition;
    private int backgroundColor;
    private long startTime;
    private double t;

    private DialogInterpolation dialogInterpolation;

    public Dialog(String text, Entity entityServer, float paddingX, float paddingY, float letterSpacing, float gap, float scale, int backgroundColor) {
        this.entityServer = entityServer;
        this.textPosition = new Vec3(entityServer.getX(), entityServer.getEyeHeight(), entityServer.getZ());
        this.paddingX = paddingX;
        this.paddingY = paddingY;
        this.backgroundColor = backgroundColor;
        this.t = 0;
        this.startTime = System.currentTimeMillis();
        this.scale = scale;
        this.opacity = 0;
        this.dialogInterpolation = new DialogInterpolation(
                textPosition.add(0, -1, 0),
                textPosition,
                scale
        );
        this.dialogScrollText = new DialogScrollText(text, letterSpacing, gap);
    }

    public void reset() {
        t = 0;
        dialogScrollText.reset();
    }

    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if(textPosition == null) return;
        updateTextPosition(deltaTracker);
        if (t < 1.0) {
            dialogInterpolation.setStartPosition(textPosition.add(0, -1, 0));
            dialogInterpolation.setEndPosition(textPosition);
            long currentTime = System.currentTimeMillis();
            t = Easing.getInterpolation(easing, Math.min((double) (currentTime - startTime) / APPEAR_TIME, 1.0));
            DialogInterpolation interpolation = dialogInterpolation.getNextValues(t);
            textPosition = interpolation.getTextPosition();
            scale = interpolation.getScale();
            opacity = interpolation.getOpacity();
        }
        Minecraft client = Minecraft.getInstance();
        fov = ((GameRendererFields)client.gameRenderer).callGetFov(
                client.gameRenderer.getMainCamera(),
                deltaTracker.getGameTimeDeltaPartialTick(true),
                true
        );
        Matrix4f projection = client.gameRenderer.getProjectionMatrix(fov);
        Matrix4f view = getViewMatrix(client.gameRenderer.getMainCamera());
        Vector4f posWorld = new Vector4f(
                (float) textPosition.x,
                (float) textPosition.y + 0.9f,
                (float) textPosition.z,
                1.0f
        );

        posClip = new Vector4f(posWorld);
        view.transform(posClip);
        projection.transform(posClip);

        float[] coord = worldToScreen(posClip);
        drawTextDialog(guiGraphics, coord[0], coord[1]);

    }

    private void updateTextPosition(DeltaTracker deltaTracker) {
        if(!entityServer.level().isClientSide) {
            for (Entity entity1 : Minecraft.getInstance().level.entitiesForRendering()) {
                if(entity1.getId() == entityServer.getId()) {
                    entityClient = entity1;
                    break;
                }
            }
        }

        if(entityClient == null) return;

        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(true);

        double x = Mth.lerp(partialTick, entityClient.xOld, entityClient.getX());
        double y = Mth.lerp(partialTick, entityClient.yOld, entityClient.getY());
        double z = Mth.lerp(partialTick, entityClient.zOld, entityClient.getZ());

        textPosition = new Vec3(x, y + entityServer.getEyeHeight(), z);

    }

    private float[] worldToScreen(Vector4f posClip) {
        Minecraft client = Minecraft.getInstance();
        if (posClip.w <= 0) return new float[]{-1000000, -1000000};

        float ndcX = posClip.x / posClip.w;
        float ndcY = posClip.y / posClip.w;
        float ndcZ = posClip.z / posClip.w;

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        float screenX = (ndcX + 1.0f) / 2.0f * screenWidth;
        float screenY = (1.0f - ndcY) / 2.0f * screenHeight;

        return new float[]{screenX, screenY};
    }

    private void drawTextDialog(GuiGraphics guiGraphics, float screenX, float screenY) {
        Minecraft client = Minecraft.getInstance();
        float resizedScale = getResizedScale();

        String text = dialogScrollText.getText();
        float letterSpacing = dialogScrollText.getLetterSpacing();
        int textLength = text.length();

        float baseTextWidth = client.font.width(text);
        float totalLetterSpacing = letterSpacing * (textLength - 1);

        float widthRectangle = ((baseTextWidth + totalLetterSpacing) / 2.0F) * resizedScale;

        float textHeight = (client.font.lineHeight * resizedScale) / 2;

        float[] coords = drawRectangle(guiGraphics, screenX, screenY, widthRectangle, textHeight, paddingX * resizedScale, paddingY * resizedScale);
        if(t >= 1.0) {
            int guiScale = client.options.guiScale().get();
            if (guiScale == 0) guiScale = 1;
            float totalHeight = coords[1];
            float centerY = screenY - (totalHeight / 2.0F);
            float textY = centerY - ((client.font.lineHeight / 2.0F) * resizedScale / 2.0F) / guiScale;
            dialogScrollText.show(guiGraphics, posClip, screenX, textY, resizedScale);
        }
    }


    private float[] drawRectangle(GuiGraphics guiGraphics, float screenX, float screenY, float width, float height, float paddingX, float paddingY) {
        Minecraft client = Minecraft.getInstance();

        int guiScale = client.options.guiScale().get();
        if (guiScale == 0) guiScale = 1;

        float totalWidth = (width + 2 * paddingX) / guiScale;
        float totalHeight = (height + 2 * paddingY) / guiScale;

        float verticalOffset = (client.font.lineHeight / 2.0f - 6.5F) * (height / client.font.lineHeight) / guiScale;
        float centerY = screenY + verticalOffset;

        float minX = screenX - totalWidth;
        float maxX = screenX + totalWidth;
        float minY = centerY - totalHeight;
        float maxY = centerY + (((client.font.lineHeight / 2.0f) * getResizedScale()) / guiScale);

        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        VertexConsumer vertexconsumer = client.renderBuffers().bufferSource().getBuffer(RenderType.gui());

        int color = (Math.min(opacity, 200) << 24) | backgroundColor;
        vertexconsumer.addVertex(matrix4f, minX, minY, 0).setColor(color);
        vertexconsumer.addVertex(matrix4f, minX, maxY, 0).setColor(color);
        vertexconsumer.addVertex(matrix4f, maxX, maxY, 0).setColor(color);
        vertexconsumer.addVertex(matrix4f, maxX, minY, 0).setColor(color);

        return new float[]{totalWidth, totalHeight};
    }

    private static Matrix4f getViewMatrix(Camera camera) {
        Vec3 camPos = camera.getPosition();
        Quaternionf camRot = camera.rotation();
        return new Matrix4f()
                .rotate(camRot.conjugate(new Quaternionf()))
                .translate((float) -camPos.x, (float) -camPos.y, (float) -camPos.z);

    }

    private float getResizedScale() {
        return (scale / posClip.w) * (70.0f / fov);
    }

    public void setTextPosition(Vec3 textPosition) {
        this.textPosition = textPosition;
    }

    public Entity getEntityServer() {
        return entityServer;
    }

    public DialogScrollText getDialogScrollText() {
        return dialogScrollText;
    }

    public void setPaddingX(float paddingX) {
        this.paddingX = paddingX;
    }

    public void setPaddingY(float paddingY) {
        this.paddingY = paddingY;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setScale(float scale) {
        this.scale = scale;
        dialogInterpolation.setScale(scale);
    }
}
