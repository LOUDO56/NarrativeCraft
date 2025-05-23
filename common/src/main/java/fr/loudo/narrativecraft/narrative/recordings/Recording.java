package fr.loudo.narrativecraft.narrative.recordings;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionDifferenceListener;
import fr.loudo.narrativecraft.narrative.recordings.actions.ActionsData;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
public class Recording {

    private final RecordingHandler recordingHandler = NarrativeCraftMod.getInstance().getRecordingHandler();
    private ServerPlayer player;

    private ActionsData actionsData;
    private ActionDifferenceListener actionDifferenceListener;
    private boolean isRecording;

    public Recording(ServerPlayer player) {
        this.player = player;
        this.actionsData = new ActionsData();
        this.isRecording = false;
    }

    public boolean start() {
        if(isRecording) return false;
        actionsData = new ActionsData();
        actionDifferenceListener = new ActionDifferenceListener(this);
        isRecording = true;
        recordingHandler.addRecording(this);
        return true;
    }

    public boolean stop() {
        if(!isRecording) return false;
        isRecording = false;
        return true;
    }

    public boolean save(Animation animation) {
        if(isRecording) return false;
        animation.setActionsData(actionsData);
        if(NarrativeCraftFile.updateAnimationFile(animation)) {
            animation.getScene().addAnimation(animation);
            return true;
        }
        return false;
    }

    public ActionsData getActionsData() {
        return actionsData;
    }

    public ActionDifferenceListener getActionDifference() {
        return actionDifferenceListener;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

}
