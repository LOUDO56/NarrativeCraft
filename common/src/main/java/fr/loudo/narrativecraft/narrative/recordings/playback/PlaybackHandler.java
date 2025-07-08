package fr.loudo.narrativecraft.narrative.recordings.playback;

import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PlaybackHandler {

    private final AtomicInteger autoId;
    private final List<Playback> playbacks;

    public PlaybackHandler() {
        this.playbacks = new ArrayList<>();
        this.autoId = new AtomicInteger();
    }

    public List<Playback> getPlaybacks() {
        return playbacks;
    }

    public void addPlayback(Playback playback) {
        playback.setId(autoId.incrementAndGet());
        if(!playbacks.contains(playback)) {
            playbacks.add(playback);
        }
    }

    public void removePlayback(Playback playback) {
        playbacks.remove(playback);
    }

    public Playback getPlayback(int id) {
        for(Playback playback : playbacks) {
            if(playback.getId() == id) {
                return playback;
            }
        }
        return null;
    }

    public boolean entityInPlayback(Entity entity) {
        for(Playback playback : playbacks) {
            if(playback.entityInPlayback(entity)) return true;
        }
        return false;
    }
}
