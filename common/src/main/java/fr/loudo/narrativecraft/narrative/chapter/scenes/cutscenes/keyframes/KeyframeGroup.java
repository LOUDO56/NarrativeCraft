package fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class KeyframeGroup {

    private int id;
    private List<Keyframe> keyframeList;

    public KeyframeGroup(int id) {
        this.id = id;
        this.keyframeList = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void showGlow(ServerPlayer player) {
        for(Keyframe keyframe : keyframeList) {
            keyframe.getCameraEntity().setGlowingTag(true);
            keyframe.updateEntityData(player);
        }
    }

    public void removeGlow(ServerPlayer player) {
        for(Keyframe keyframe : keyframeList) {
            keyframe.getCameraEntity().setGlowingTag(false);
            keyframe.updateEntityData(player);
        }
    }

    public void showLineBetweenKeyframes(PoseStack poseStack) {
        Minecraft client = Minecraft.getInstance();
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        PoseStack.Pose matrix4f = poseStack.last();
        VertexConsumer vertexConsumer = client.renderBuffers().bufferSource().getBuffer(RenderType.debugLineStrip(1.0F));

        for (int i = 0; i < keyframeList.size() - 1; i++) {
            Keyframe firstKeyFrame = keyframeList.get(i);
            Keyframe secondKeyFrame = keyframeList.get(i + 1);

            KeyframeCoordinate startPos = firstKeyFrame.getKeyframeCoordinate();
            KeyframeCoordinate endPos = secondKeyFrame.getKeyframeCoordinate();
            double x1 = startPos.getX() - cameraPos.x;
            double y1 = startPos.getY() - cameraPos.y;
            double z1 = startPos.getZ() - cameraPos.z;
            double x2 = endPos.getX() - cameraPos.x;
            double y2 = endPos.getY() - cameraPos.y;
            double z2 = endPos.getZ() - cameraPos.z;

            vertexConsumer.addVertex(matrix4f, new Vector3f((float) x1, (float) y1, (float) z1))
                    .setColor(1.0F, 1.0F, 0.0F, 1.0F)
                    .setNormal(0, 1, 0);
            vertexConsumer.addVertex(matrix4f, new Vector3f((float) x2, (float) y2, (float) z2))
                    .setColor(1.0F, 1.0F, 0.0F, 1.0F)
                    .setNormal(0, 1, 0);

        }
        client.renderBuffers().bufferSource().endBatch();
    }

    public long getTotalDuration() {
        long totalDuration = 0;
        for(Keyframe keyframe : keyframeList) {
            totalDuration += keyframe.getPathTime();
        }
        return totalDuration;
    }

    public List<Keyframe> getKeyframeList() {
        return keyframeList;
    }

    public void setId(int id) {
        this.id = id;
    }
}
