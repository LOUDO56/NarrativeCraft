package fr.loudo.narrativecraft;

import fr.loudo.narrativecraft.narrative.chapter.ChapterManager;
import fr.loudo.narrativecraft.narrative.character.CharacterManager;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.RecordingHandler;
import fr.loudo.narrativecraft.narrative.session.PlayerSessionManager;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(NarrativeCraft.MODID)
public class NarrativeCraft {

    public static final String MODID = "narrativecraft";
    public static final Logger LOGGER = LogManager.getLogger();
    private static NarrativeCraft instance = new NarrativeCraft();
    private ChapterManager chapterManager;
    private CharacterManager characterManager;
    private PlayerSessionManager playerSessionManager;
    private RecordingHandler recordingHandler;

    public NarrativeCraft() {
        chapterManager = new ChapterManager();
        characterManager = new CharacterManager();
        recordingHandler = new RecordingHandler();
        playerSessionManager = new PlayerSessionManager();
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
}
