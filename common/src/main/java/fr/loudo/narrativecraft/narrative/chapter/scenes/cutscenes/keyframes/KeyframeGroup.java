package fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

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

    public void showLineBetweenKeyframes(ServerPlayer player) {
        for (int i = 0; i < keyframeList.size() - 1; i++) {
            Keyframe firstKeyFrame = keyframeList.get(i);
            Keyframe secondKeyFrame = keyframeList.get(i + 1);

            KeyframeCoordinate startPos = firstKeyFrame.getKeyframeCoordinate();
            KeyframeCoordinate endPos = secondKeyFrame.getKeyframeCoordinate();

            Vec3 vec3StartPos = new Vec3(startPos.getX(), startPos.getY(), startPos.getZ());
            Vec3 vec3EndPos = new Vec3(endPos.getX(), endPos.getY(), endPos.getZ());

            double distance = vec3StartPos.distanceTo(vec3EndPos);

            int numParticles = (int) (distance / 0.5);

            for (int j = 0; j <= numParticles; j++) {
                double t = j / (double) numParticles;

                Vec3 particlePos = vec3StartPos.add(vec3EndPos.subtract(vec3StartPos).scale(t));

                player.connection.send(new ClientboundLevelParticlesPacket(
                        ParticleTypes.CRIT,
                        false,
                        false,
                        particlePos.x(),
                        particlePos.y(),
                        particlePos.z(),
                        0f,
                        0f,
                        0f,
                        0,
                        3
                ));
            }
        }
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
