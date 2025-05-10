package fr.loudo.narrativecraft;

import com.bladecoder.ink.runtime.Story;
import fr.loudo.narrativecraft.narrative.chapter.ChapterManager;
import fr.loudo.narrativecraft.narrative.character.CharacterManager;
import fr.loudo.narrativecraft.narrative.recordings.RecordingHandler;
import fr.loudo.narrativecraft.narrative.recordings.playback.PlaybackHandler;
import fr.loudo.narrativecraft.narrative.session.PlayerSessionManager;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NarrativeCraftMod {

    public static final String MOD_ID = "narrativecraft";
    public static final String MOD_NAME = "NarrativeCraft";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    public static Story story;
    public static MinecraftServer server;
    private static final NarrativeCraftMod instance = new NarrativeCraftMod();

    static {
        try {
            story = new Story("{\"inkVersion\":21,\"root\":[[\"#\",\"^animation play chapter-1.village.village_jake\",\"/#\",\"^Once upon a time...\",\"\\n\",[\"done\",{\"#f\":5,\"#n\":\"g-0\"}],null],\"done\",{\"#f\":1}],\"listDefs\":{}}");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ChapterManager chapterManager;
    private CharacterManager characterManager;
    private PlayerSessionManager playerSessionManager;
    private RecordingHandler recordingHandler;
    private PlaybackHandler playbackHandler;

    public NarrativeCraftMod() {
        chapterManager = new ChapterManager();
        characterManager = new CharacterManager();
        recordingHandler = new RecordingHandler();
        playerSessionManager = new PlayerSessionManager();
        playbackHandler = new PlaybackHandler();
    }

    public static NarrativeCraftMod getInstance() {
        return instance;
    }

    public ChapterManager getChapterManager() {
        return chapterManager;
    }

    public CharacterManager getCharacterManager() {
        return characterManager;
    }

    public PlayerSessionManager getPlayerSessionManager() {
        return playerSessionManager;
    }

    public RecordingHandler getRecordingHandler() {
        return recordingHandler;
    }

    public PlaybackHandler getPlaybackHandler() {
        return playbackHandler;
    }
}
