package fr.loudo.narrativecraft.narrative.recordings;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RecordingHandler {

    public static final AtomicInteger ids = new AtomicInteger();

    private List<Recording> recordings;

    public RecordingHandler() {
        this.recordings = new ArrayList<>();
    }

    public List<Recording> getRecordings() {
        return recordings;
    }

    public boolean addRecording(Recording recording) {
        for(Recording recording1 : recordings) {
            if(recording.getId() == recording1.getId()) {
                return false;
            }
        }
        recordings.add(recording);
        return true;
    }

    public boolean removeRecording(Recording recording) {
        return recordings.removeIf(recording1 -> recording1.getId() == recording.getId());
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
