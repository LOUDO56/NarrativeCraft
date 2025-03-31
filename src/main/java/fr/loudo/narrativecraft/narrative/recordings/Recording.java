package fr.loudo.narrativecraft.narrative.recordings;

import fr.loudo.narrativecraft.NarrativeCraft;
import net.minecraft.server.level.ServerPlayer;

public class Recording {

    private final RecordingHandler recordingHandler = NarrativeCraft.getInstance().getRecordingHandler();
    private ServerPlayer player;
    private boolean isRecording;

    public Recording(ServerPlayer player) {
        this.player = player;
        this.isRecording = false;
    }

    public boolean start() {
        if(isRecording) return false;
        recordingHandler.getRecordings().add(this);
        isRecording = true;
        return true;
    }

    public boolean stop() {
        if(!isRecording) return false;
        recordingHandler.getRecordings().remove(this);
        isRecording = false;
        return true;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

}
