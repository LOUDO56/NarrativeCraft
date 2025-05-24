package fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeGroup;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.utils.Easing;
import fr.loudo.narrativecraft.utils.MathUtils;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeCoordinate;
import fr.loudo.narrativecraft.utils.TpUtil;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class CutscenePlayback  {

    private double t;
    private long startTime, startDelay, transitionDelay, endTime, defaultEndTime, pauseStartTime, totalPausedTime;
    private boolean isPaused;
    private int currentIndexKeyframe, currentIndexKeyframeGroup;
    private KeyframeCoordinate currentLoc;
    private Keyframe firstKeyframe, secondKeyframe;
    private List<KeyframeGroup> keyframeGroupList;
    private KeyframeGroup currentKeyframeGroup;
    private ServerPlayer player;
    private PlayerSession playerSession;
    private CutsceneController cutsceneController;
    private Runnable onCutsceneEnd;

    public CutscenePlayback(ServerPlayer player, List<KeyframeGroup> keyframeGroupList, CutsceneController cutsceneController) {
        this.player = player;
        this.keyframeGroupList = keyframeGroupList;
        this.currentKeyframeGroup = keyframeGroupList.getFirst();
        this.firstKeyframe = currentKeyframeGroup.getKeyframeList().getFirst();
        this.secondKeyframe = currentKeyframeGroup.getKeyframeList().size() == 1 ? null : currentKeyframeGroup.getKeyframeList().get(currentKeyframeGroup.getKeyframeList().size() + 1);
        this.currentIndexKeyframe = 0;
        this.currentIndexKeyframeGroup = 0;
        this.playerSession = Utils.getSessionOrNull(player);
        this.cutsceneController = cutsceneController;
        initValues();
    }

    public CutscenePlayback(ServerPlayer player, List<KeyframeGroup> keyframeGroupList, Keyframe keyframe, CutsceneController cutsceneController) {
        this.player = player;
        this.keyframeGroupList = keyframeGroupList;
        this.cutsceneController = cutsceneController;
        playerSession = Utils.getSessionOrNull(player);
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
        next();
        playerSession.setCutscenePlayback(this);
        cutsceneController.resume();
        StoryHandler.changePlayerCutsceneMode(player, cutsceneController.getPlaybackType(), true);
    }

    public void stop() {
        KeyframeCoordinate lastPos = secondKeyframe.getKeyframeCoordinate();
        TpUtil.teleportPlayer(player, lastPos.getX(), lastPos.getY(), lastPos.getZ());
        cutsceneController.pause();
        if(cutsceneController.getPlaybackType() == Playback.PlaybackType.DEVELOPMENT) {
            cutsceneController.setCurrentPreviewKeyframe(secondKeyframe, true);
        } else {
            cutsceneController.stopSession();
        }
        playerSession.setCutscenePlayback(null);
        if(onCutsceneEnd != null) {
            onCutsceneEnd.run();
        }
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
        long currentTime = System.currentTimeMillis();
        Minecraft minecraft = Minecraft.getInstance();
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
        t = Easing.getInterpolation(secondKeyframe.getEasing(), Math.min((double) elapsedTime / endTime, 1.0));
        currentLoc = getNextPosition(firstKeyframe.getKeyframeCoordinate(), secondKeyframe.getKeyframeCoordinate(), t);
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
