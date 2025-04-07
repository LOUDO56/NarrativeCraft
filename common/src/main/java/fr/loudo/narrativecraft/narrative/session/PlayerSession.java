package fr.loudo.narrativecraft.narrative.session;

import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.scenes.Scene;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class PlayerSession {

    private ServerPlayer player;
    private Chapter chapter;
    private Scene scene;
    private List<Subscene> subscenesPlaying;
    private boolean overwriteState;

    public PlayerSession(ServerPlayer player) {
        this.player = player;
        this.subscenesPlaying = new ArrayList<>();
        this.overwriteState = false;
    }

    public PlayerSession(ServerPlayer player, Chapter chapter, Scene scene) {
        this.player = player;
        this.overwriteState = false;
    }

    public ServerPlayer getPlayer() {
        return player;
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
}
