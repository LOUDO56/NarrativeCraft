package fr.loudo.narrativecraft.narrative.recordings.playback;

import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PlaybackHandler {

    public static final AtomicInteger ids = new AtomicInteger();
    private final List<Playback> playbacks;

    public PlaybackHandler() {
        this.playbacks = new ArrayList<>();
    }

    public List<Playback> getPlaybacks() {
        return playbacks;
    }

    public void addPlayback(Playback playback) {
        for(Playback playback1 : playbacks) {
            if(playback1.getId() == playback.getId()) {
                return;
            }
        }
        playbacks.add(playback);
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
