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

    public CutscenePlayback(ServerPlayer player, List<KeyframeGroup> keyframeGroupList) {
        this.player = player;
        this.keyframeGroupList = keyframeGroupList;
        this.currentKeyframeGroup = keyframeGroupList.getFirst();
        this.firstKeyframe = currentKeyframeGroup.getKeyframeList().getFirst();
        this.secondKeyframe = currentKeyframeGroup.getKeyframeList().size() == 1 ? null : currentKeyframeGroup.getKeyframeList().get(currentKeyframeGroup.getKeyframeList().size() + 1);
        this.currentIndexKeyframe = 0;
        this.currentIndexKeyframeGroup = 0;
        this.playerSession = Utils.getSessionOrNull(player);
    }

    public CutscenePlayback(ServerPlayer player, List<KeyframeGroup> keyframeGroupList, KeyframeGroup currentKeyframeGroup, Keyframe keyframe) {
        this.player = player;
        this.keyframeGroupList = keyframeGroupList;
        for(int i = 0; i < currentKeyframeGroup.getKeyframeList().size(); i++) {
            if(currentKeyframeGroup.getKeyframeList().get(i).getId() == keyframe.getId()) {
                this.currentIndexKeyframe = i;
            }
        }
        this.currentKeyframeGroup = currentKeyframeGroup;
        this.currentIndexKeyframeGroup = currentKeyframeGroup.getId() - 1;
        this.firstKeyframe = currentKeyframeGroup.getKeyframeList().get(currentIndexKeyframe);
        if(currentIndexKeyframe < currentKeyframeGroup.getKeyframeList().size()) {
            this.secondKeyframe =  currentKeyframeGroup.getKeyframeList().get(currentIndexKeyframe + 1);
        }
        this.playerSession = Utils.getSessionOrNull(player);
    }

    public void initStartFrame() {
        t = 0;
        startTime = System.currentTimeMillis();
        duration = currentKeyframeGroup.getTotalDuration();
        playerSession.getCutsceneController().resume();
    }

    public void stop() {
        KeyframeCoordinate lastPos = secondKeyframe.getKeyframeCoordinate();
        Minecraft.getInstance().options.hideGui = false;
        TpUtil.teleportPlayer(player, lastPos.getX(), lastPos.getY(), lastPos.getZ());
        playerSession.setCutscenePlayback(null);
        playerSession.getCutsceneController().pause();
    }

    public KeyframeCoordinate next() {
        if(startTime == 0) {
            initStartFrame();
        }
        Minecraft.getInstance().options.hideGui = true;

        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;
        System.out.println(currentTime);
        System.out.println(elapsedTime);
        System.out.println(duration);
        System.out.println(elapsedTime / duration);
        t = Math.min(elapsedTime / duration, 1.0);
        double smoothedT = Easings.easeOut(t);
        if(t >= 1.0) {
            currentIndexKeyframe++;
            if(currentIndexKeyframe == currentKeyframeGroup.getKeyframeList().size() - 1) {
                if(currentIndexKeyframeGroup != keyframeGroupList.size() - 1) {
                    currentIndexKeyframeGroup++;
                    currentIndexKeyframe = 0;
                    currentKeyframeGroup = keyframeGroupList.get(currentIndexKeyframeGroup);
                } else {
                    stop();
                }
            }
            initStartFrame();
        }
        currentLoc = getNextPosition(firstKeyframe.getKeyframeCoordinate(), secondKeyframe.getKeyframeCoordinate(), smoothedT);
        return currentLoc;
    }

    private KeyframeCoordinate getNextPosition(KeyframeCoordinate position1, KeyframeCoordinate position2, double t) {
        double x = MathUtils.lerp(position1.getX(), position2.getX(), t);
        double y = MathUtils.lerp(position1.getY(), position2.getY(), t);
        double z = MathUtils.lerp(position1.getZ(), position2.getZ(), t);
        float XRot = (float) MathUtils.lerp(position1.getXRot(), position2.getXRot(), t);
        float YRot = (float) MathUtils.lerp(position1.getYRot(), position2.getYRot(), t);
        int fov = (int) MathUtils.lerp(position1.getFov(), position2.getFov(), t);
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
