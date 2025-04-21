package fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeGroup;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.utils.Easings;
import fr.loudo.narrativecraft.utils.MathUtils;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeCoordinate;
import fr.loudo.narrativecraft.utils.TpUtil;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class CutscenePlayback  {

    private double t;

    private long startTime;
    private long delay;
    private double duration;
    private ServerPlayer player;
    private PlayerSession playerSession;
    private KeyframeGroup currentKeyframeGroup;
    private List<KeyframeGroup> keyframeGroupList;
    private int currentIndexKeyframe;
    private int currentIndexKeyframeGroup;
    private Keyframe firstKeyframe;
    private Keyframe secondKeyframe;
    private KeyframeCoordinate currentLoc;
    private CutsceneController cutsceneController;

    public CutscenePlayback(ServerPlayer player, List<KeyframeGroup> keyframeGroupList) {
        this.player = player;
        this.keyframeGroupList = keyframeGroupList;
        this.currentKeyframeGroup = keyframeGroupList.getFirst();
        this.firstKeyframe = currentKeyframeGroup.getKeyframeList().getFirst();
        this.secondKeyframe = currentKeyframeGroup.getKeyframeList().size() == 1 ? null : currentKeyframeGroup.getKeyframeList().get(currentKeyframeGroup.getKeyframeList().size() + 1);
        this.currentIndexKeyframe = 0;
        this.currentIndexKeyframeGroup = 0;
        this.playerSession = Utils.getSessionOrNull(player);
        this.cutsceneController = playerSession.getCutsceneController() == null ? null : playerSession.getCutsceneController();
    }

    public CutscenePlayback(ServerPlayer player, List<KeyframeGroup> keyframeGroupList, Keyframe keyframe) {
        this.player = player;
        this.keyframeGroupList = keyframeGroupList;
        for(KeyframeGroup keyframeGroup : keyframeGroupList) {
            for(Keyframe keyframeFromGroup : keyframeGroup.getKeyframeList()) {
                if(keyframeFromGroup.getId() == keyframe.getId()) {
                    this.currentKeyframeGroup = keyframeGroup;
                    break;
                }
            }
        }
        for(int i = 0; i < currentKeyframeGroup.getKeyframeList().size(); i++) {
            if(currentKeyframeGroup.getKeyframeList().get(i).getId() == keyframe.getId()) {
                this.currentIndexKeyframe = i;
            }
        }
        this.currentIndexKeyframeGroup = currentKeyframeGroup.getId() - 1;
        if(currentIndexKeyframe == currentKeyframeGroup.getKeyframeList().size() - 1) {
            currentIndexKeyframe = 0;
            currentIndexKeyframeGroup++;
            currentKeyframeGroup = keyframeGroupList.get(currentIndexKeyframeGroup);
            firstKeyframe = currentKeyframeGroup.getKeyframeList().getFirst();
            if(currentKeyframeGroup.getKeyframeList().size() > 1) {
                secondKeyframe = currentKeyframeGroup.getKeyframeList().get(1);
            } else {
                secondKeyframe = firstKeyframe;
            }
        } else {
            firstKeyframe = currentKeyframeGroup.getKeyframeList().get(currentIndexKeyframe);
            secondKeyframe = currentKeyframeGroup.getKeyframeList().get(currentIndexKeyframe + 1);
        }

        this.playerSession = Utils.getSessionOrNull(player);
        this.cutsceneController = playerSession.getCutsceneController() == null ? null : playerSession.getCutsceneController();
    }

    public void initStartFrame() {
        t = 0;
        startTime = System.currentTimeMillis();
        if(secondKeyframe.isParentGroup()) {
            delay = System.currentTimeMillis() + secondKeyframe.getStartDelay();
        } else {
            delay = System.currentTimeMillis() + firstKeyframe.getStartDelay();
        }
        duration = currentKeyframeGroup.getTotalDuration();
        cutsceneController.resume();
    }

    public void stop() {
        KeyframeCoordinate lastPos = secondKeyframe.getKeyframeCoordinate();
        TpUtil.teleportPlayer(player, lastPos.getX(), lastPos.getY(), lastPos.getZ());
        cutsceneController.pause();
        cutsceneController.setCurrentPreviewKeyframe(secondKeyframe);
        playerSession.setCutscenePlayback(null);
    }

    public KeyframeCoordinate next() {
        if(startTime == 0) {
            initStartFrame();
        }
        Minecraft.getInstance().options.hideGui = true;

        long currentTime = System.currentTimeMillis();
        if(currentTime < delay) {
            startTime = System.currentTimeMillis();
            // If next keyframe is the first keyframe of the next group and has a start delay, then wait at the last keyframe of the last group
            if(firstKeyframe.isParentGroup() && currentIndexKeyframeGroup > 0) {
                currentLoc = keyframeGroupList.get(currentIndexKeyframeGroup - 1).getKeyframeList().getLast().getKeyframeCoordinate();
            } else {
                currentLoc = firstKeyframe.getKeyframeCoordinate();
            }
            return currentLoc;
        }
        long elapsedTime = currentTime - startTime;
        t = Math.min((double) elapsedTime / secondKeyframe.getPathTime(), 1.0);
        double smoothedT = Easings.easeOut(t);
        currentLoc = getNextPosition(firstKeyframe.getKeyframeCoordinate(), secondKeyframe.getKeyframeCoordinate(), t);
        if (t >= 1.0) {
            currentIndexKeyframe++;
            if (currentIndexKeyframe >= currentKeyframeGroup.getKeyframeList().size() - 1) {
                currentIndexKeyframeGroup++;
                if (currentIndexKeyframeGroup >= keyframeGroupList.size()) {
                    stop();
                    return secondKeyframe.getKeyframeCoordinate();
                } else {
                    currentKeyframeGroup = keyframeGroupList.get(currentIndexKeyframeGroup);
                    currentIndexKeyframe = 0;
                }

            }
            firstKeyframe = currentKeyframeGroup.getKeyframeList().get(currentIndexKeyframe);
            if(currentKeyframeGroup.getKeyframeList().size() > 1) {
                secondKeyframe = currentKeyframeGroup.getKeyframeList().get(currentIndexKeyframe + 1);
            } else {
                secondKeyframe = firstKeyframe;
            }
            initStartFrame();
        }
        return currentLoc;
    }

    private KeyframeCoordinate getNextPosition(KeyframeCoordinate position1, KeyframeCoordinate position2, double t) {
        double x = MathUtils.lerp(position1.getX(), position2.getX(), t);
        double y = MathUtils.lerp(position1.getY(), position2.getY(), t);
        double z = MathUtils.lerp(position1.getZ(), position2.getZ(), t);
        float XRot = (float) MathUtils.lerp(position1.getXRot(), position2.getXRot(), t);
        float YRot = (float) MathUtils.lerp(position1.getYRot(), position2.getYRot(), t);
        float fov = (float) MathUtils.lerp(position1.getFov(), position2.getFov(), t);
        return new KeyframeCoordinate(x, y, z, XRot, YRot, fov);
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public KeyframeGroup getCurrentKeyframeGroup() {
        return currentKeyframeGroup;
    }

    public List<KeyframeGroup> getKeyframeGroupList() {
        return keyframeGroupList;
    }

    public KeyframeCoordinate getCurrentLoc() {
        return currentLoc;
    }
}
