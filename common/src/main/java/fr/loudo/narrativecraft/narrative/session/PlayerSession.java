package fr.loudo.narrativecraft.narrative.session;

import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scenes.KeyframeControllerBase;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutscenePlayback;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeCoordinate;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;

import java.util.ArrayList;
import java.util.List;

public class PlayerSession {

    private transient List<Subscene> subscenesPlaying;
    private transient CutscenePlayback cutscenePlayback;
    private transient boolean overwriteState;
    private Chapter chapter;
    private Scene scene;
    private KeyframeControllerBase keyframeControllerBase;
    private KeyframeCoordinate soloCam;

    public PlayerSession() {
        this.subscenesPlaying = new ArrayList<>();
        this.overwriteState = false;
    }

    public PlayerSession(Chapter chapter, Scene scene) {
        this.chapter = chapter;
        this.scene = scene;
        this.subscenesPlaying = new ArrayList<>();
        this.overwriteState = false;
    }

    public boolean sessionSet() {
        return chapter != null && scene != null;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public boolean isOverwriteState() {
        return overwriteState;
    }

    public void setOverwriteState(boolean overwriteState) {
        this.overwriteState = overwriteState;
    }

    public List<Subscene> getSubscenesPlaying() {
        return subscenesPlaying;
    }

    public void setKeyframeControllerBase(KeyframeControllerBase keyframeControllerBase) {
        this.keyframeControllerBase = keyframeControllerBase;
    }

    public KeyframeControllerBase getKeyframeControllerBase() {
        return keyframeControllerBase;
    }

    public CutscenePlayback getCutscenePlayback() {
        return cutscenePlayback;
    }

    public void setCutscenePlayback(CutscenePlayback cutscenePlayback) {
        this.cutscenePlayback = cutscenePlayback;
    }

    public KeyframeCoordinate getSoloCam() {
        return soloCam;
    }

    public void setSoloCam(KeyframeCoordinate soloCam) {
        this.soloCam = soloCam;
    }

    public void reset() {
        chapter = null;
        scene = null;
        subscenesPlaying.clear();
        keyframeControllerBase = null;
        cutscenePlayback = null;
        overwriteState = false;
        soloCam = null;
    }
}
