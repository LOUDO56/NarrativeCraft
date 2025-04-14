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

            Vec3 startPos = firstKeyFrame.getPosition();
            Vec3 endPos = secondKeyFrame.getPosition();

            int numParticles = 15;

            for (int j = 0; j <= numParticles; j++) {
                double t = j / (double) numParticles;

                Vec3 particlePos = startPos.add(endPos.subtract(startPos).scale(t));

                player.connection.send(new ClientboundLevelParticlesPacket(
                        ParticleTypes.CRIT,
                        false,
                        false,
                        particlePos.x(),
                        particlePos.y() + 1,
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


    public List<Keyframe> getKeyframeList() {
        return keyframeList;
    }

}
