package fr.loudo.narrativecraft.narrative.recordings;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.recordings.actions.ActionsData;
import fr.loudo.narrativecraft.narrative.recordings.actions.GameModeAction;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionDifferenceListener;
import fr.loudo.narrativecraft.narrative.recordings.actions.modsListeners.ModsListenerImpl;
import net.minecraft.server.level.ServerPlayer;

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
        GameModeAction gameModeAction = new GameModeAction(0, player.gameMode.getGameModeForPlayer(), player.gameMode.getGameModeForPlayer());
        actionsData.addAction(gameModeAction);
        return true;
    }

    public boolean stop() {
        if(!isRecording) return false;
        isRecording = false;
        for(ModsListenerImpl modsListener : actionDifferenceListener.getModsListenerList()) {
            modsListener.stop();
        }
        actionsData.reset(player);
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

    public int getTick() {
        return actionDifferenceListener.getTick();
    }
}
