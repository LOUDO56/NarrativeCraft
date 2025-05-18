package fr.loudo.narrativecraft.narrative.chapter.scenes;

import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeGroup;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public abstract class KeyframeControllerBase {

    protected List<KeyframeGroup> keyframeGroups;
    protected final ServerPlayer player;
    protected final List<Entity> keyframesEntity;
    protected Keyframe currentPreviewKeyframe;
    protected Playback.PlaybackType playbackType;

    public KeyframeControllerBase(List<KeyframeGroup> keyframeGroups, ServerPlayer player, Playback.PlaybackType playbackType) {
        this.keyframeGroups = keyframeGroups;
        this.player = player;
        this.keyframesEntity = new ArrayList<>();
        this.playbackType = playbackType;
    }

    public KeyframeControllerBase(KeyframeGroup keyframeGroup, ServerPlayer player, Playback.PlaybackType playbackType) {
        this.keyframeGroups = List.of(keyframeGroup);
        this.player = player;
        this.keyframesEntity = new ArrayList<>();
        this.playbackType = playbackType;
    }

    public abstract void startSession();
    public abstract void stopSession();
    public abstract boolean addKeyframe();
    public abstract boolean removeKeyframe(Keyframe keyframe);

    public Keyframe getNextKeyframe(Keyframe current) {
        for (int i = 0; i < keyframeGroups.size(); i++) {
            KeyframeGroup group = keyframeGroups.get(i);
            List<Keyframe> keyframes = group.getKeyframeList();
            for (int j = 0; j < keyframes.size(); j++) {
                if (keyframes.get(j).getId() == current.getId()) {
                    if (j + 1 < keyframes.size()) {
                        return keyframes.get(j + 1);
                    } else if (i + 1 < keyframeGroups.size()) {
                        return keyframeGroups.get(i + 1).getKeyframeList().getFirst();
                    }
                }
            }
        }
        return null;
    }

    public Keyframe getPreviousKeyframe(Keyframe current) {
        for (int i = 0; i < keyframeGroups.size(); i++) {
            KeyframeGroup group = keyframeGroups.get(i);
            List<Keyframe> keyframes = group.getKeyframeList();
            for (int j = 0; j < keyframes.size(); j++) {
                if (keyframes.get(j).getId() == current.getId()) {
                    if (j > 0) {
                        return keyframes.get(j - 1);
                    } else if (i > 0) {
                        return keyframeGroups.get(i - 1).getKeyframeList().getLast();
                    }
                }
            }
        }
        return null;
    }

    public boolean isFirstKeyframe(KeyframeGroup group, Keyframe keyframe) {
        return group.getKeyframeList().getFirst().getId() == keyframe.getId();
    }

    public boolean isLastKeyframe(KeyframeGroup group, Keyframe keyframe) {
        return group.getKeyframeList().getLast().getId() == keyframe.getId();
    }

    public KeyframeGroup getKeyframeGroupByKeyframe(Keyframe keyframe) {
        for (KeyframeGroup group : keyframeGroups) {
            for (Keyframe k : group.getKeyframeList()) {
                if (k.getId() == keyframe.getId()) {
                    return group;
                }
            }
        }
        return null;
    }

    public int getKeyframeIndex(KeyframeGroup group, Keyframe keyframe) {
        List<Keyframe> keyframes = group.getKeyframeList();
        for (int i = 0; i < keyframes.size(); i++) {
            if (keyframes.get(i).getId() == keyframe.getId()) {
                return i;
            }
        }
        return -1;
    }

    public Keyframe getKeyframeByEntity(Entity entity) {
        for(KeyframeGroup keyframeGroup : keyframeGroups) {
            for(Keyframe keyframe : keyframeGroup.getKeyframeList()) {
                if(keyframe.getCameraEntity().getId() == entity.getId()) {
                    return keyframe;
                }
            }
        }
        return null;
    }

    public List<KeyframeGroup> getKeyframeGroups() {
        return keyframeGroups;
    }

    public void setKeyframeGroups(List<KeyframeGroup> keyframeGroups) {
        this.keyframeGroups = keyframeGroups;
    }

    public Keyframe getCurrentPreviewKeyframe() {
        return currentPreviewKeyframe;
    }

    public Playback.PlaybackType getPlaybackType() {
        return playbackType;
    }
}