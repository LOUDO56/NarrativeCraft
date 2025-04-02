package fr.loudo.narrativecraft.narrative.recordings;

import fr.loudo.narrativecraft.NarrativeCraft;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.animations.Animation;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Recording {

    private final RecordingHandler recordingHandler = NarrativeCraft.getInstance().getRecordingHandler();
    private ServerPlayer player;
    private List<MovementData> movementData;
    private boolean isRecording;

    public Recording(ServerPlayer player) {
        this.player = player;
        this.movementData = new ArrayList<>();
        this.isRecording = false;
    }

    public boolean start() {
        if(isRecording) return false;
        movementData = new ArrayList<>();
        recordingHandler.getRecordings().add(this);
        isRecording = true;
        return true;
    }

    public boolean stop() {
        if(!isRecording) return false;
        isRecording = false;
        return true;
    }

    public boolean save(Animation animation) {
        if(isRecording) return false;
        try {
            animation.setLocations(movementData);
            recordingHandler.getRecordings().remove(this);
            NarrativeCraftFile.saveAnimation(animation);
            return true;
        } catch (IOException e) {
            throw new RuntimeException("Error while saving record animation: " + e);
        }
    }

    public List<MovementData> getLocations() {
        return movementData;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

}
