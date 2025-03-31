package fr.loudo.narrativecraft.narrative.recordings;

import fr.loudo.narrativecraft.NarrativeCraft;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.animations.Animation;
import fr.loudo.narrativecraft.utils.Location;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Recording {

    private final RecordingHandler recordingHandler = NarrativeCraft.getInstance().getRecordingHandler();
    private ServerPlayer player;
    private List<Location> locations;
    private boolean isRecording;

    public Recording(ServerPlayer player) {
        this.player = player;
        this.locations = new ArrayList<>();
        this.isRecording = false;
    }

    public boolean start() {
        if(isRecording) return false;
        locations = new ArrayList<>();
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
            animation.setLocations(locations);
            recordingHandler.getRecordings().remove(this);
            NarrativeCraftFile.saveAnimation(animation);
            return true;
        } catch (IOException e) {
            throw new RuntimeException("Error while saving record animation: " + e);
        }
    }

    public List<Location> getLocations() {
        return locations;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

}
