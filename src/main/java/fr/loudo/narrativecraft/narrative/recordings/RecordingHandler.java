package fr.loudo.narrativecraft.narrative.recordings;

import net.minecraft.server.level.ServerPlayer;

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

    public Recording getRecordingOfPlayer(ServerPlayer player) {
        for(Recording recording : recordings) {
            if(recording.getPlayer().getName().getString().equals(player.getName().getString())) {
                return recording;
            }
        }
        return null;
    }

    public boolean isPlayerRecording(ServerPlayer player) {
        for(Recording recording : recordings) {
            if(recording.getPlayer().getName().getString().equals(player.getName().getString())) {
                return true;
            }
        }
        return false;
    }
}
