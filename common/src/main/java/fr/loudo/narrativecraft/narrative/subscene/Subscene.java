package fr.loudo.narrativecraft.narrative.subscene;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.animations.Animation;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.scenes.Scene;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class Subscene {

    private transient Scene scene;
    private transient List<Playback> playbackList;
    private String name;
    private String sceneName;
    private List<String> animationStringList;

    public Subscene(String name, Scene scene) {
        this.name = name;
        this.scene = scene;
        this.sceneName = scene.getName();
        this.animationStringList = new ArrayList<>();
        this.playbackList = new ArrayList<>();
    }

    public boolean addAnimation(String animationName) {
        if(animationStringList.contains(animationName.toLowerCase())) return false;
        animationStringList.add(animationName.toLowerCase());
        return true;
    }

    public boolean removeAnimation(String animationName) {
        if(!animationStringList.contains(animationName.toLowerCase())) return false;
        animationStringList.remove(animationName.toLowerCase());
        return true;
    }

    public void start(ServerPlayer player) {
        playbackList = new ArrayList<>();
        for(String animationName : animationStringList) {
            Animation animation = NarrativeCraftFile.getAnimationFromFile(scene.getChapter().getIndex(), scene.getName(), animationName);
            Playback playback = new Playback(animation, player.serverLevel());
            playback.start();
            playbackList.add(playback);
        }
    }

    public void stop() {
        for(Playback playback : playbackList) {
            playback.stop();
        }
        playbackList = new ArrayList<>();
    }

}
