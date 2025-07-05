package fr.loudo.narrativecraft.narrative.recordings;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class RecordingHandler {
    private List<Recording> recordings;

    public RecordingHandler() {
        this.recordings = new ArrayList<>();
    }

    public List<Recording> getRecordings() {
        return recordings;
    }

    public boolean addRecording(Recording recording) {
        if(recordings.contains(recording)) return false;
        recordings.add(recording);
        return true;
    }

    public boolean removeRecording(Recording recording) {
        if(!recordings.contains(recording)) return false;
        recordings.remove(recording);
        return true;
    }

    public Recording getRecordingOfPlayer(Player player) {
        for(Recording recording : recordings) {
            if(recording.getEntity().getName().getString().equals(player.getName().getString())) {
                return recording;
            }
        }
        return null;
    }

    public Recording getRecordingOfPlayer(ServerPlayer player) {
        for(Recording recording : recordings) {
            if(recording.getEntity().getName().getString().equals(player.getName().getString())) {
                return recording;
            }
        }
        return null;
    }

    public boolean isPlayerRecording(ServerPlayer player) {
        for(Recording recording : recordings) {
            if(recording.getEntity().getName().getString().equals(player.getName().getString()) && recording.isRecording()) {
                return true;
            }
        }
        return false;
    }
}
