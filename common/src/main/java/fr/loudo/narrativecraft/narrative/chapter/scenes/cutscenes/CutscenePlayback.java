package fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeCoordinate;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeGroup;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.utils.Easing;
import fr.loudo.narrativecraft.utils.MathUtils;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class CutscenePlayback  {

    private double t;
    private long startTime, startDelay, transitionDelay, endTime, defaultEndTime, pauseStartTime, totalPausedTime;
    private final PlayerSession playerSession;
    private boolean isPaused;
    private int currentIndexKeyframe, currentIndexKeyframeGroup;
    private KeyframeCoordinate currentLoc;
    private Keyframe firstKeyframe, secondKeyframe;
    private List<KeyframeGroup> keyframeGroupList;
    private KeyframeGroup currentKeyframeGroup;
    private ServerPlayer player;
    private CutsceneController cutsceneController;
    private Runnable onCutsceneEnd;

    public CutscenePlayback(ServerPlayer player, List<KeyframeGroup> keyframeGroupList, CutsceneController cutsceneController) {
        playerSession = NarrativeCraftMod.getInstance().getPlayerSession();
        this.player = player;
        this.keyframeGroupList = keyframeGroupList;
        this.currentKeyframeGroup = keyframeGroupList.getFirst();
        this.firstKeyframe = currentKeyframeGroup.getKeyframeList().getFirst();
        this.secondKeyframe = currentKeyframeGroup.getKeyframeList().size() == 1 ? null : currentKeyframeGroup.getKeyframeList().get(currentKeyframeGroup.getKeyframeList().size() + 1);
        this.currentIndexKeyframe = 0;
        this.currentIndexKeyframeGroup = 0;
        this.cutsceneController = cutsceneController;
        initValues();
    }

    public CutscenePlayback(ServerPlayer player, List<KeyframeGroup> keyframeGroupList, Keyframe keyframe, CutsceneController cutsceneController) {
        playerSession = NarrativeCraftMod.getInstance().getPlayerSession();
        this.player = player;
        this.keyframeGroupList = keyframeGroupList;
        this.cutsceneController = cutsceneController;
        currentKeyframeGroup = cutsceneController.getKeyframeGroupByKeyframe(keyframe);
        currentIndexKeyframe = cutsceneController.getKeyframeIndex(currentKeyframeGroup, keyframe);
        currentIndexKeyframeGroup = currentKeyframeGroup.getId() - 1;
        initFrames();

    }

    private void initFrames() {
        firstKeyframe = currentKeyframeGroup.getKeyframeList().get(currentIndexKeyframe);
        if(currentKeyframeGroup.getKeyframeList().getLast().getId() == firstKeyframe.getId()) {
            secondKeyframe = firstKeyframe;
        } else {
            secondKeyframe = currentKeyframeGroup.getKeyframeList().get(currentIndexKeyframe + 1);
        }
        initValues();
    }

    private void nextFrame() {
        currentIndexKeyframe++;
        if(cutsceneController.isLastKeyframe(currentKeyframeGroup, secondKeyframe)) {
            currentIndexKeyframe = 0;
            currentIndexKeyframeGroup++;
            currentKeyframeGroup = keyframeGroupList.get(currentIndexKeyframeGroup);
        }
        initFrames();

    }

    public void start() {
        playerSession.setCutscenePlayback(this);
        cutsceneController.resume();
        StoryHandler.changePlayerCutsceneMode(cutsceneController.getPlaybackType(), true);
        next();
    }

    public void stop() {
        KeyframeCoordinate lastPos = secondKeyframe.getKeyframeCoordinate();
        Minecraft.getInstance().player.setPos(lastPos.getVec3());
        cutsceneController.pause();
        playerSession.setCutscenePlayback(null);
        if(cutsceneController.getPlaybackType() == Playback.PlaybackType.DEVELOPMENT) {
            cutsceneController.setCurrentPreviewKeyframe(secondKeyframe, true);
        } else {
            cutsceneController.stopSession(false);
            if(onCutsceneEnd != null) {
                onCutsceneEnd.run();
            }
        }
    }

    public void skip() {
        Keyframe lastKeyframe = keyframeGroupList.getLast().getKeyframeList().getLast();
        int lastTick = (int) (lastKeyframe.getTick() + ((lastKeyframe.getTransitionDelay() / 1000L) * 20));
        cutsceneController.changeTimePosition(lastTick, true);
        stop();
    }

    private void initValues() {
        totalPausedTime = 0;
        startTime = System.currentTimeMillis();
        endTime = secondKeyframe.getPathTime();
        startDelay = startTime + firstKeyframe.getStartDelay();
        transitionDelay = startDelay + secondKeyframe.getTransitionDelay();
        if(secondKeyframe.getId() != firstKeyframe.getId()) {
            transitionDelay += endTime;
        }
        defaultEndTime = startTime + endTime + firstKeyframe.getStartDelay() + secondKeyframe.getTransitionDelay();
        if(secondKeyframe.getSpeed() > 0) {
            endTime = (long) (endTime / secondKeyframe.getSpeed());
        }
        if(secondKeyframe.getId() == firstKeyframe.getId()) {
            endTime = 0;
        }
    }

    public KeyframeCoordinate next() {
        Minecraft minecraft = Minecraft.getInstance();
        long currentTime = System.currentTimeMillis();
        if(minecraft.options.keyShift.isDown() && cutsceneController.getPlaybackType() == Playback.PlaybackType.DEVELOPMENT) {
            currentLoc = firstKeyframe.getKeyframeCoordinate();
            cutsceneController.pause();
            cutsceneController.setCurrentPreviewKeyframe(firstKeyframe, false);
            playerSession.setCutscenePlayback(null);
            return currentLoc;
        }
        if(minecraft.isPaused() && !isPaused) {
            isPaused = true;
            pauseStartTime = currentTime;
        } else if(!minecraft.isPaused() && isPaused) {
            isPaused = false;
            totalPausedTime += currentTime - pauseStartTime;
        }
        if(isPaused) {
            return currentLoc;
        }
        long adjustedTime = currentTime - totalPausedTime;
        long elapsedTime = adjustedTime - startTime;
        if (adjustedTime < startDelay) {
            startTime = adjustedTime;
            currentLoc = firstKeyframe.getKeyframeCoordinate();
            return currentLoc;
        }
        if(secondKeyframe.getEasing() == Easing.SMOOTH) {
            List<Keyframe> keyframes = currentKeyframeGroup.getKeyframeList();
            currentLoc = getInterpolatedPosition(keyframes, firstKeyframe, elapsedTime);
        } else {
            t = Easing.getInterpolation(secondKeyframe.getEasing(), Math.min((double) elapsedTime / endTime, 1.0));
            currentLoc = getNextPosition(firstKeyframe.getKeyframeCoordinate(), secondKeyframe.getKeyframeCoordinate(), t);
        }
        if(t >= 1.0 && adjustedTime >= transitionDelay || adjustedTime >= defaultEndTime) {
            if(cutsceneController.isLastKeyframe(secondKeyframe)) {
                stop();
            } else {
                nextFrame();
            }
        }
        return currentLoc;
    }

    private KeyframeCoordinate getNextPosition(KeyframeCoordinate position1, KeyframeCoordinate position2, double t) {
        double x = MathUtils.lerp(position1.getX(), position2.getX(), t);
        double y = MathUtils.lerp(position1.getY(), position2.getY(), t);
        double z = MathUtils.lerp(position1.getZ(), position2.getZ(), t);
        float XRot = (float) MathUtils.lerp(position1.getXRot(), position2.getXRot(), t);
        float YRot = getAccurateYaw(position1.getYRot(), position2.getYRot(), t);
        float ZRot = getAccurateRotation(position1.getZRot(), position2.getZRot(), t);
        float fov = (float) MathUtils.lerp(position1.getFov(), position2.getFov(), t);
        return new KeyframeCoordinate(x, y, z, XRot, YRot, ZRot, fov);
    }

    private float getAccurateRotation(float startAngle, float endAngle, double t) {
        float diff = endAngle - startAngle;
        if (diff > 180) {
            diff -= 360;
        } else if (diff < -180) {
            diff += 360;
        }
        return startAngle + (float)(diff * t);
    }

    private float getAccurateYaw(float startAngle, float endAngle, double t) {
        float diff = endAngle - startAngle;

        if (diff > 180) {
            diff -= 360;
        } else if (diff < -180) {
            diff += 360;
        }

        float interpolated = startAngle + (float)(diff * t);

        if (interpolated > 180) {
            interpolated -= 360;
        } else if (interpolated < -180) {
            interpolated += 360;
        }

        return interpolated;
    }

    private float interpolateAngleCatmullRom(float a0, float a1, float a2, float a3, double t) {
        a0 = unwrapAngle(a0, a1);
        a2 = unwrapAngle(a2, a1);
        a3 = unwrapAngle(a3, a2);

        double t2 = t * t;
        double t3 = t2 * t;

        float angle = (float) (0.5 * ((2.0 * a1)
                + (-a0 + a2) * t
                + (2.0 * a0 - 5.0 * a1 + 4.0 * a2 - a3) * t2
                + (-a0 + 3.0 * a1 - 3.0 * a2 + a3) * t3));

        return wrapAngle(angle);
    }

    private float unwrapAngle(float angle, float reference) {
        float diff = angle - reference;
        while (diff < -180) diff += 360;
        while (diff > 180) diff -= 360;
        return reference + diff;
    }

    private float wrapAngle(float angle) {
        while (angle <= -180) angle += 360;
        while (angle > 180) angle -= 360;
        return angle;
    }

    public KeyframeCoordinate getInterpolatedPosition(List<Keyframe> keyframes, Keyframe firstKeyframe, long elapsedTime) {
        if (keyframes.size() < 2) return keyframes.getFirst().getKeyframeCoordinate();
        int startIndex = 0;
        for(Keyframe keyframe : keyframes) {
            if(keyframe.getId() == firstKeyframe.getId()) {
                break;
            }
            startIndex++;
        }

        long accumulatedTime = 0;
        for (int i = startIndex; i < keyframes.size() - 1; i++) {
            Keyframe k1 = keyframes.get(i);
            Keyframe k2 = keyframes.get(i + 1);

            long segmentDuration = (long) (k2.getPathTime() / k2.getSpeed());
            if (elapsedTime < accumulatedTime + segmentDuration) {
                t = (double) (elapsedTime - accumulatedTime) / segmentDuration;

                Keyframe p0 = keyframes.get(Math.max(i - 1, 0));
                Keyframe p1 = k1;
                Keyframe p2 = k2;
                Keyframe p3 = keyframes.get(Math.min(i + 2, keyframes.size() - 1));

                return interpolateCatmullRom(
                        p0.getKeyframeCoordinate(),
                        p1.getKeyframeCoordinate(),
                        p2.getKeyframeCoordinate(),
                        p3.getKeyframeCoordinate(),
                        t
                );
            }

            accumulatedTime += segmentDuration;
        }

        return keyframes.getLast().getKeyframeCoordinate();
    }


    private KeyframeCoordinate interpolateCatmullRom(KeyframeCoordinate p0, KeyframeCoordinate p1, KeyframeCoordinate p2, KeyframeCoordinate p3, double t) {
        double t2 = t * t;
        double t3 = t2 * t;

        double x = 0.5 * ((2.0 * p1.getX())
                + (-p0.getX() + p2.getX()) * t
                + (2.0 * p0.getX() - 5.0 * p1.getX() + 4.0 * p2.getX() - p3.getX()) * t2
                + (-p0.getX() + 3.0 * p1.getX() - 3.0 * p2.getX() + p3.getX()) * t3);

        double y = 0.5 * ((2.0 * p1.getY())
                + (-p0.getY() + p2.getY()) * t
                + (2.0 * p0.getY() - 5.0 * p1.getY() + 4.0 * p2.getY() - p3.getY()) * t2
                + (-p0.getY() + 3.0 * p1.getY() - 3.0 * p2.getY() + p3.getY()) * t3);

        double z = 0.5 * ((2.0 * p1.getZ())
                + (-p0.getZ() + p2.getZ()) * t
                + (2.0 * p0.getZ() - 5.0 * p1.getZ() + 4.0 * p2.getZ() - p3.getZ()) * t2
                + (-p0.getZ() + 3.0 * p1.getZ() - 3.0 * p2.getZ() + p3.getZ()) * t3);

        float XRot = (float) (0.5 * ((2.0 * p1.getXRot())
                        + (-p0.getXRot() + p2.getXRot()) * t
                        + (2.0 * p0.getXRot() - 5.0 * p1.getXRot() + 4.0 * p2.getXRot() - p3.getXRot()) * t2
                        + (-p0.getXRot() + 3.0 * p1.getXRot() - 3.0 * p2.getXRot() + p3.getXRot()) * t3));

        float YRot = interpolateAngleCatmullRom(
                p0.getYRot(),
                p1.getYRot(),
                p2.getYRot(),
                p3.getYRot(),
                t
        );

        float ZRot = interpolateAngleCatmullRom(
                p0.getZRot(),
                p1.getZRot(),
                p2.getZRot(),
                p3.getZRot(),
                t
        );

        float fov = (float) (0.5 * ((2.0 * p1.getFov())
                + (-p0.getFov() + p2.getFov()) * t
                + (2.0 * p0.getFov() - 5.0 * p1.getFov() + 4.0 * p2.getFov() - p3.getFov()) * t2
                + (-p0.getFov() + 3.0 * p1.getFov() - 3.0 * p2.getFov() + p3.getFov()) * t3));

        return new KeyframeCoordinate(x, y, z, XRot, YRot, ZRot, fov);
    }


    public ServerPlayer getPlayer() {
        return player;
    }

    public KeyframeGroup getCurrentKeyframeGroup() {
        return currentKeyframeGroup;
    }

    public int getCurrentIndexKeyframe() {
        return currentIndexKeyframe;
    }

    public List<KeyframeGroup> getKeyframeGroupList() {
        return keyframeGroupList;
    }

    public KeyframeCoordinate getCurrentLoc() {
        return currentLoc;
    }

    public Runnable getOnCutsceneEnd() {
        return onCutsceneEnd;
    }

    public void setOnCutsceneEnd(Runnable onCutsceneEnd) {
        this.onCutsceneEnd = onCutsceneEnd;
    }



}
