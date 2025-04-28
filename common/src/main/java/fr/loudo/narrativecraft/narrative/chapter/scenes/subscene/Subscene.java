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

    private transient Scene scene;
    private transient List<Playback> playbackList;
    private String name;
    private List<String> animationStringList;

    public Subscene(String name, Scene scene) {
        this.name = name;
        this.scene = scene;
        this.animationStringList = new ArrayList<>();
        this.playbackList = new ArrayList<>();
    }

    public boolean addAnimation(String animationName) {
        if(animationStringList.contains(animationName.toLowerCase())) return false;
        try {
            animationStringList.add(animationName.toLowerCase());
            NarrativeCraftFile.saveChapter(scene.getChapter());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean removeAnimation(String animationName) {
        if(!animationStringList.contains(animationName.toLowerCase())) return false;
        try {
            animationStringList.remove(animationName.toLowerCase());
            NarrativeCraftFile.saveChapter(scene.getChapter());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean animationExists(String animationName) {
        for(String animation : animationStringList) {
            if(animation.equalsIgnoreCase(animationName)) {
                return true;
            }
        }
        return false;
    }

    public void start(ServerPlayer player) {
        if(playbackList == null) {
            playbackList = new ArrayList<>();
        }
        for(String animationName : animationStringList) {
            Animation animation = NarrativeCraftFile.getAnimationFromFile(scene.getChapter().getIndex(), scene.getName(), animationName);
            Playback playback = new Playback(animation, player.serverLevel());
            playback.start();
            playbackList.add(playback);
        }
    }

    public void stop() {
        for(Playback playback : playbackList) {
            playback.stopAndKill();
        }
        playbackList.clear();
    }

    public String getName() {
        return name;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public List<Playback> getPlaybackList() {
        return playbackList;
    }
}
