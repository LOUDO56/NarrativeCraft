package fr.loudo.narrativecraft.narrative.playback;

import java.util.ArrayList;
import java.util.List;

public class PlaybackHandler {

    private List<Playback> playbacks;

    public PlaybackHandler() {
        this.playbacks = new ArrayList<>();
    }

    public List<Playback> getPlaybacks() {
        return playbacks;
    }
}
