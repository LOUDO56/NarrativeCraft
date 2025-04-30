package fr.loudo.narrativecraft.narrative.dialog;

import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.loudo.narrativecraft.mixin.fields.GameRendererFields;
import fr.loudo.narrativecraft.utils.Easing;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
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

    private Vector4f posClip;
    private float fov, paddingX, paddingY, scale, opacity;

    private Entity entity;
    private Vec3 textPosition, lastPos, currentPos;
    private int backgroundColor;
    private long startTime;
    private double t;

    private DialogInterpolation dialogInterpolation;

    public Dialog(Entity entity, float paddingX, float paddingY, int backgroundColor) {
        this.entity = entity;
        this.textPosition = new Vec3(entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ());
        this.lastPos = textPosition;
        this.paddingX = paddingX;
        this.paddingY = paddingY;
        this.backgroundColor = backgroundColor;
        this.t = 0;
        this.startTime = System.currentTimeMillis();
        this.scale = 15f;
        this.opacity = 0f;
        this.dialogInterpolation = new DialogInterpolation(
                this,
                new Vec3(textPosition.x, textPosition.y - 1.5f, textPosition.z),
                textPosition,
                scale
        );
    }

    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if(textPosition == null) return;
        updateTextPosition(deltaTracker);
        if (t < 1.0) {
            dialogInterpolation.setStartPosition(new Vec3(textPosition.x, textPosition.y - 1.5f, textPosition.z));
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
        drawTextDialog(guiGraphics, "FORTNITE R34 FEET", coord[0], coord[1], scale);

    }

    private void updateTextPosition(DeltaTracker deltaTracker) {
        for (Entity entity1 : Minecraft.getInstance().level.entitiesForRendering()) {
            if(entity1.getId() == entity.getId()) {
                double x = Mth.lerp(deltaTracker.getGameTimeDeltaPartialTick(true), entity1.xOld, entity1.getX());
                double y = Mth.lerp(deltaTracker.getGameTimeDeltaPartialTick(true), entity1.yOld, entity1.getY());
                double z = Mth.lerp(deltaTracker.getGameTimeDeltaPartialTick(true), entity1.zOld, entity1.getZ());
                textPosition = new Vec3(x, y + entity.getBbHeight(), z);
                break;
            }
        }
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

    private void drawTextDialog(GuiGraphics guiGraphics, String text, float screenX, float screenY, float scale) {
        Minecraft client = Minecraft.getInstance();

        float resizedScale = getResizedScale(scale);
        float textWidth = (client.font.width(text) * resizedScale);
        float textHeight = ((client.font.lineHeight) * resizedScale);

        drawRectangle(guiGraphics, screenX, screenY, textWidth, textHeight, paddingX * resizedScale, paddingY * resizedScale);
        drawString(guiGraphics, text, screenX, screenY, resizedScale);
    }

    private float[] drawRectangle(GuiGraphics guiGraphics, float screenX, float screenY, float width, float height, float paddingX, float paddingY) {
        Minecraft client = Minecraft.getInstance();

        int guiScale = client.options.guiScale().get();
        if (guiScale == 0) guiScale = 1;

        float totalWidth = (width + 2 * paddingX) / guiScale;
        float totalHeight = (height + 2 * paddingY) / guiScale;

        float verticalOffset = (client.font.lineHeight / 2.0f - 5.5f) * (height / client.font.lineHeight) / guiScale;
        float centerY = screenY + verticalOffset;

        float halfWidth = totalWidth / 2.0f;
        float halfHeight = totalHeight / 2.0f;

        float minX = screenX - halfWidth;
        float maxX = screenX + halfWidth;
        float minY = centerY - halfHeight;
        float maxY = centerY + halfHeight;

        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        VertexConsumer vertexconsumer = client.renderBuffers().bufferSource().getBuffer(RenderType.gui());

        float a;
        if(t <= 1.0) {
            a = opacity;
        } else {
            a = (backgroundColor >> 24) & 0xFF;
        }
        float r = (backgroundColor >> 16) & 0xFF;
        float g = (backgroundColor >> 8) & 0xFF;
        float b = backgroundColor & 0xFF;

        vertexconsumer.addVertex(matrix4f, minX, minY, 0).setColor(r, g, b, a);
        vertexconsumer.addVertex(matrix4f, minX, maxY, 0).setColor(r, g, b, a);
        vertexconsumer.addVertex(matrix4f, maxX, maxY, 0).setColor(r, g, b, a);
        vertexconsumer.addVertex(matrix4f, maxX, minY, 0).setColor(r, g, b, a);

        return new float[]{minX, minY, maxX, maxY};
    }

    private float drawString(GuiGraphics guiGraphics, String text, float screenX, float screenY, float scale) {
        Minecraft client = Minecraft.getInstance();
        MultiBufferSource.BufferSource buffers = client.renderBuffers().bufferSource();

        if (posClip.w <= 0) return 0;

        guiGraphics.pose().pushPose();
        int guiScale = client.options.guiScale().get();
        if (guiScale == 0) guiScale = 1;
        scale /= guiScale;
        guiGraphics.pose().scale(scale, scale, 2.0f);

        client.font.drawInBatch(
                text,
                (screenX / scale) - ((float) client.font.width(text) / 2),
                (screenY / scale) - ((float) client.font.lineHeight / 2),
                0xFFFFFF,
                false,
                guiGraphics.pose().last().pose(),
                buffers,
                Font.DisplayMode.NORMAL,
                0,
                15728880
        );

        guiGraphics.pose().popPose();
        buffers.endBatch();

        return client.font.width(text);
    }

    private static Matrix4f getViewMatrix(Camera camera) {
        Vec3 camPos = camera.getPosition();
        Quaternionf camRot = camera.rotation();
        return new Matrix4f()
                .rotate(camRot.conjugate(new Quaternionf()))
                .translate((float) -camPos.x, (float) -camPos.y, (float) -camPos.z);

    }

    private float getResizedScale(float baseScale) {
        return (baseScale / posClip.w) * (70.0f / fov);
    }

    public void setTextPosition(Vec3 textPosition) {
        this.textPosition = textPosition;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setLastPos(Vec3 lastPos) {
        this.lastPos = lastPos;
    }

    public void setCurrentPos(Vec3 currentPos) {
        this.currentPos = currentPos;
    }
}
