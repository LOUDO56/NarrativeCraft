package fr.loudo.narrativecraft.narrative.recordings;

import fr.loudo.narrativecraft.NarrativeCraftManager;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.animations.Animation;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionDifference;
import fr.loudo.narrativecraft.narrative.recordings.actions.ActionsData;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
public class Recording {

    private final RecordingHandler recordingHandler = NarrativeCraftManager.getInstance().getRecordingHandler();
    private ServerPlayer player;

    private ActionsData actionsData;
    private ActionDifference actionDifference;
    private boolean isRecording;
    private int tickAction;

    public Recording(ServerPlayer player) {
        this.player = player;
        this.actionsData = new ActionsData();
        this.actionDifference = new ActionDifference(this);
        this.isRecording = false;
        this.tickAction = 0;
    }

    public boolean start() {
        if(isRecording) return false;
        actionsData = new ActionsData();
        recordingHandler.getRecordings().add(this);
        isRecording = true;
        return true;
    }

    public boolean stop() {
        if(!isRecording) return false;
        isRecording = false;
        return true;
    }

    public void addTickAction() {
        tickAction++;
    }

    public boolean save(Animation animation) {
        if(isRecording) return false;
        try {
            animation.setActionsData(actionsData);
            recordingHandler.getRecordings().remove(this);
            NarrativeCraftFile.saveAnimation(animation);
            return true;
        } catch (IOException e) {
            throw new RuntimeException("Error while saving record animation: " + e);
        }
    }

    public ActionsData getActionsData() {
        return actionsData;
    }

    public ActionDifference getActionDifference() {
        return actionDifference;
    }

    public int getTickAction() {
        return tickAction;
    }

    public void setTickAction(int tickAction) {
        this.tickAction = tickAction;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

}
