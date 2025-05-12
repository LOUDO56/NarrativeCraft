package fr.loudo.narrativecraft.narrative.chapter.scenes.subscene;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.screens.story_manager.StoryDetails;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Subscene extends StoryDetails {

    private transient Scene scene;
    private transient List<Animation> animationList;
    private transient List<Playback> playbackList;
    private List<String> animationNameList;

    public Subscene(String name, String description, Scene scene) {
        super(name, description);
        this.scene = scene;
        this.animationList = new ArrayList<>();
        this.animationNameList = new ArrayList<>();
        this.playbackList = new ArrayList<>();
    }

    public void start(ServerPlayer player) {
    }

    public void stop() {
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public Scene getScene() {
        return scene;
    }

    public List<Animation> getAnimationList() {
        return animationList;
    }

    public List<Playback> getPlaybackList() {
        return playbackList;
    }

    public List<String> getAnimationNameList() {
        return animationNameList;
    }
}
