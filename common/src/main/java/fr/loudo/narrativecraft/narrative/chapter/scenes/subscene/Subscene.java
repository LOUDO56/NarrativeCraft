package fr.loudo.narrativecraft.narrative.chapter.scenes.subscene;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Subscene {

    private transient List<Playback> playbackList;
    private String name;
    private List<String> animationStringList;


    public List<Playback> getPlaybackList() {
        return playbackList;
    }

    public void setPlaybackList(List<Playback> playbackList) {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getAnimationStringList() {
        return animationStringList;
    }

    public void setAnimationStringList(List<String> animationStringList) {
        this.animationStringList = animationStringList;
    }

    public void start(ServerPlayer player) {
    }

    public void stop() {
    }
}
