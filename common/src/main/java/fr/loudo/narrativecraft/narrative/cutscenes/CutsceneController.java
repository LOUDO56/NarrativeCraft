package fr.loudo.narrativecraft.narrative.cutscenes;

import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import net.minecraft.server.level.ServerPlayer;

public class CutsceneController {

    private Cutscene cutscene;
    private ServerPlayer player;
    private boolean isPlaying;
    private int currentTick;

    public CutsceneController(Cutscene cutscene, ServerPlayer player) {
        this.cutscene = cutscene;
        this.player = player;
        this.isPlaying = false;
        this.currentTick = 0;
    }

    public void startSession() {

        for(Subscene subscene : cutscene.getSubsceneList()) {
            subscene.start(player);
        }

        pause();

    }

    public void stopSession() {

        for(Subscene subscene : cutscene.getSubsceneList()) {
            subscene.stop();
        }

        isPlaying = false;

    }

    public void pause() {
        isPlaying = false;
        changePlayingPlaybackState();
    }

    public void resume() {
        isPlaying = true;
        changePlayingPlaybackState();
    }

    public void changeTimePosition(int newTick) {
        currentTick = newTick;
        for(Subscene subscene : cutscene.getSubsceneList()) {
            for(Playback playback : subscene.getPlaybackList()) {
                playback.changeLocationByTick(newTick);
            }
        }
    }

    public void next() {
        if(isPlaying) currentTick++;
    }

    private void changePlayingPlaybackState() {
        for(Subscene subscene : cutscene.getSubsceneList()) {
            for(Playback playback : subscene.getPlaybackList()) {
                playback.setPlaying(isPlaying);
            }
        }
    }

    public Cutscene getCutscene() {
        return cutscene;
    }
}
