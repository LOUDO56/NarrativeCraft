package fr.loudo.narrativecraft.narrative.session;

import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.scenes.Scene;
import net.minecraft.server.level.ServerPlayer;

public class PlayerSession {

    private ServerPlayer player;
    private Chapter chapter;
    private Scene scene;

    public PlayerSession(ServerPlayer player) {
        this.player = player;
    }

    public PlayerSession(ServerPlayer player, Chapter chapter, Scene scene) {
        this.player = player;
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
}
