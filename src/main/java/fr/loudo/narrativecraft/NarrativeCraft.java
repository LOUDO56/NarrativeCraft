package fr.loudo.narrativecraft;

import com.bladecoder.ink.runtime.Story;
import fr.loudo.narrativecraft.narrative.chapter.ChapterManager;
import fr.loudo.narrativecraft.narrative.character.CharacterManager;
import fr.loudo.narrativecraft.narrative.playback.PlaybackHandler;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.RecordingHandler;
import fr.loudo.narrativecraft.narrative.session.PlayerSessionManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(NarrativeCraft.MODID)
public class NarrativeCraft {

    public static final String MODID = "narrativecraft";
    public static final Logger LOGGER = LogManager.getLogger();
    public static MinecraftServer server;
    public static Story story;
    private static NarrativeCraft instance = new NarrativeCraft();

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

    public NarrativeCraft() {
        chapterManager = new ChapterManager();
        characterManager = new CharacterManager();
        recordingHandler = new RecordingHandler();
        playerSessionManager = new PlayerSessionManager();
        playbackHandler = new PlaybackHandler();
    }

    public static NarrativeCraft getInstance() {
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
