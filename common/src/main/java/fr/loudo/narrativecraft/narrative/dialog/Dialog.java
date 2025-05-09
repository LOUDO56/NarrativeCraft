package fr.loudo.narrativecraft.narrative.dialog;

import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.loudo.narrativecraft.mixin.fields.GameRendererFields;
import fr.loudo.narrativecraft.narrative.dialog.animations.DialogAnimationArrowSkip;
import fr.loudo.narrativecraft.narrative.dialog.animations.DialogAppearAnimation;
import fr.loudo.narrativecraft.narrative.dialog.animations.DialogAnimationScrollText;
import fr.loudo.narrativecraft.narrative.dialog.geometrics.DialogueTail;
import fr.loudo.narrativecraft.utils.Easing;
import fr.loudo.narrativecraft.utils.MathUtils;
import fr.loudo.narrativecraft.utils.ScreenUtils;
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
    private final long DIALOG_CHANGE_TIME = 500L;
    private final Easing easing = Easing.SMOOTH;

    private DialogAnimationScrollText dialogAnimationScrollText;
    private DialogAnimationArrowSkip dialogAnimationArrowSkip;
    private DialogueTail dialogueTail;
    private Vector4f posClip;
    private float fov, paddingX, paddingY, scale, oldWidth, oldHeight, oldScale, oldPaddingX, oldPaddingY, oldMaxWidth, tailXPoint, tailYPoint;
    private int opacity;
    private boolean acceptNewDialog;

    private Entity entityServer;
    private Entity entityClient;
    private Vec3 textPosition;
    private int backgroundColor;
    private long startTime;
    private double t; // Used to make transtions between two points

    private DialogAppearAnimation dialogAppearAnimation;

    public Dialog(String text, Entity entityServer, float paddingX, float paddingY, float letterSpacing, float gap, float scale, int backgroundColor, int maxWidth) {
        this.entityServer = entityServer;
        this.textPosition = new Vec3(entityServer.getX(), entityServer.getEyeHeight(), entityServer.getZ());
        this.paddingX = paddingX;
        this.paddingY = paddingY * 2;
        this.backgroundColor = backgroundColor;
        this.t = 0;
        this.startTime = System.currentTimeMillis();
        this.scale = scale;
        this.opacity = 0;
        this.dialogAppearAnimation = new DialogAppearAnimation(
                textPosition.add(0, -1, 0),
                textPosition,
                scale
        );
        this.dialogAnimationScrollText = new DialogAnimationScrollText(text, letterSpacing, gap, maxWidth);
        this.dialogAnimationArrowSkip = new DialogAnimationArrowSkip(5f, 3f, 10f, -5f, 400L, 0xFFFFFF, 80, Easing.SMOOTH);
        this.acceptNewDialog = false;
        this.dialogueTail = new DialogueTail(10f, 5f);
    }

    public void reset() {
        dialogAnimationArrowSkip = new DialogAnimationArrowSkip(5f, 3f, 5f, -5f, 200L, 0xFFFFFF, 80, Easing.SMOOTH);
        dialogueTail = new DialogueTail(7f, 15f);
        acceptNewDialog = true;
        startTime = System.currentTimeMillis();
        t = 0;
        dialogAnimationScrollText.reset();
    }

    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if(textPosition == null) return;
        updateTextPosition(deltaTracker);
        if (t < 1.0 && !acceptNewDialog) {
            //TODO: interpolate by transform and scale, not position.
            dialogAppearAnimation.setStartPosition(textPosition.add(0, -1, 0));
            dialogAppearAnimation.setEndPosition(textPosition);
            long currentTime = System.currentTimeMillis();
            t = Easing.getInterpolation(easing, Math.min((double) (currentTime - startTime) / APPEAR_TIME, 1.0));
            DialogAppearAnimation interpolation = dialogAppearAnimation.getNextValues(t);
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
        posClip = new Vector4f(
                (float) textPosition.x,
                (float) textPosition.y + 0.9f,
                (float) textPosition.z,
                1.0f
        );
        view.transform(posClip);
        projection.transform(posClip);

        Vector4f tailClip = new Vector4f(
                (float) textPosition.x,
                (float) textPosition.y + 0.9f,
                (float) textPosition.z,
                1.0f
        );
        view.transform(tailClip);
        projection.transform(tailClip);

        float[] coord = worldToScreen(posClip);
        tailXPoint = worldToScreen(tailClip)[0];
        tailYPoint =  worldToScreen(tailClip)[1];
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
        float resizedScale = getResizedScale(scale);
        float maxWidth = dialogAnimationScrollText.getMaxWidthLine();
        if(t >= 1.0) {
            oldScale = scale;
            oldMaxWidth = maxWidth;
        } else {
            if(oldScale != scale || oldMaxWidth != maxWidth) {
                resizedScale = (float) MathUtils.lerp(getResizedScale(oldScale), getResizedScale(scale), t);
                maxWidth = (float) MathUtils.lerp(oldMaxWidth, maxWidth, t);
            }
        }

        float widthRectangle = maxWidth / 2.0F;
        float heightRectangle = dialogAnimationScrollText.getTotalHeight();

        dialogAnimationScrollText.init(screenX, screenY, paddingY, resizedScale);

        float[] coords = drawBackground(
                guiGraphics,
                screenX,
                screenY,
                widthRectangle,
                heightRectangle,
                paddingX,
                paddingY,
                resizedScale
        );
        int color = (opacity << 24) | backgroundColor;
        dialogueTail.draw(guiGraphics, color, tailXPoint, tailYPoint, coords[0], coords[1], coords[2], coords[3], resizedScale);
        if (t >= 1.0) {
            dialogAnimationScrollText.show(guiGraphics, posClip);
            if(dialogAnimationScrollText.isFinished()) {
                dialogAnimationArrowSkip.draw(guiGraphics, coords[1], coords[3], resizedScale, posClip);
            } else {
                dialogAnimationArrowSkip.reset();
            }
        }

    }

    private float[] drawBackground(GuiGraphics guiGraphics, float x, float y, float width, float height, float paddingX, float paddingY, float resizedScale) {
        Minecraft client = Minecraft.getInstance();

        float pixelPaddingX = ScreenUtils.getPixelValue(paddingX, resizedScale);
        float pixelPaddingY = ScreenUtils.getPixelValue(paddingY, resizedScale);

        float totalWidth = ScreenUtils.getPixelValue(width, resizedScale) + 2 * pixelPaddingX;
        float totalHeight = ScreenUtils.getPixelValue(height, resizedScale) + 2 * pixelPaddingY;

        if (t >= 1.0) {
            oldWidth = width;
            oldHeight = height;
            oldPaddingX = paddingX;
            oldPaddingY = paddingY;
        } else {
            if(oldWidth == width && oldHeight == height && oldPaddingX == paddingX && oldPaddingY == paddingY && oldScale == scale && oldMaxWidth == dialogAnimationScrollText.getMaxWidthLine()) {
                t = 1.0;
            } else {
                // All scaled stuff is to interpolate and match the player's distance and fov (or else during the interpolation, width and height remains the same on UI).
                float scaledOldPixelPaddingX = ScreenUtils.getPixelValue(oldPaddingX, resizedScale);
                float scaledOldPixelPaddingY = ScreenUtils.getPixelValue(oldPaddingY, resizedScale);
                float scaledOldWidth = ScreenUtils.getPixelValue(oldWidth, resizedScale) + 2 * scaledOldPixelPaddingX;
                float scaledOldHeight = ScreenUtils.getPixelValue(oldHeight, resizedScale) + 2 * scaledOldPixelPaddingY;
                long currentTime = System.currentTimeMillis();
                totalWidth = (float) MathUtils.lerp(scaledOldWidth, totalWidth, t);
                totalHeight = (float) MathUtils.lerp(scaledOldHeight, totalHeight, t);
                t = Easing.getInterpolation(easing, Math.min((double) (currentTime - startTime) / DIALOG_CHANGE_TIME, 1.0));
            }
        }

        float minX = x - totalWidth;
        float maxX = x + totalWidth;
        float minY = y - totalHeight;
        float maxY = y;

        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        VertexConsumer vertexconsumer = client.renderBuffers().bufferSource().getBuffer(RenderType.gui());

        int color = (opacity << 24) | backgroundColor;
        vertexconsumer.addVertex(matrix4f, minX, minY, 0).setColor(color);
        vertexconsumer.addVertex(matrix4f, minX, maxY, 0).setColor(color);
        vertexconsumer.addVertex(matrix4f, maxX, maxY, 0).setColor(color);
        vertexconsumer.addVertex(matrix4f, maxX, minY, 0).setColor(color);

        client.renderBuffers().bufferSource().endBatch();

        return new float[]{minX, maxX, minY, maxY};
    }



    private static Matrix4f getViewMatrix(Camera camera) {
        Vec3 camPos = camera.getPosition();
        Quaternionf camRot = camera.rotation();
        return new Matrix4f()
                .rotate(camRot.conjugate(new Quaternionf()))
                .translate((float) -camPos.x, (float) -camPos.y, (float) -camPos.z);

    }

    private float getResizedScale(float scale) {
        return (scale / posClip.w) * (70.0f / fov);
    }

    public void setTextPosition(Vec3 textPosition) {
        this.textPosition = textPosition;
    }

    public Entity getEntityServer() {
        return entityServer;
    }

    public DialogAnimationScrollText getDialogScrollText() {
        return dialogAnimationScrollText;
    }

    public void setPaddingX(float paddingX) {
        this.paddingX = paddingX;
    }

    public void setPaddingY(float paddingY) {
        this.paddingY = paddingY * 2;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setScale(float scale) {
        this.scale = scale;
        dialogAppearAnimation.setScale(scale);
    }
}
