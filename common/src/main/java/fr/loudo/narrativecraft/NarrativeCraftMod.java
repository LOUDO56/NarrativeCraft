package fr.loudo.narrativecraft;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.ChapterManager;
import fr.loudo.narrativecraft.narrative.character.CharacterManager;
import fr.loudo.narrativecraft.narrative.dialog.Dialog;
import fr.loudo.narrativecraft.narrative.dialog.Dialog2d;
import fr.loudo.narrativecraft.narrative.recordings.RecordingHandler;
import fr.loudo.narrativecraft.narrative.recordings.playback.PlaybackHandler;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.screens.mainScreen.NarrativeCraftLogoRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NarrativeCraftMod {

    private static final NarrativeCraftMod instance = new NarrativeCraftMod();
    public static final String MOD_ID = "narrativecraft";
    public static final String MOD_NAME = "NarrativeCraft";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    public static MinecraftServer server;

    private StoryHandler storyHandler;
    private boolean isCutsceneMode;
    private final ChapterManager chapterManager;
    private final CharacterManager characterManager;
    private final RecordingHandler recordingHandler;
    private final PlaybackHandler playbackHandler;
    private final PlayerSession playerSession;
    private final NarrativeCraftLogoRenderer narrativeCraftLogoRenderer;
    private NarrativeUserOptions narrativeUserOptions;
    private Dialog testDialog;
    private Dialog2d testDialog2d;

    public NarrativeCraftMod() {
        chapterManager = new ChapterManager();
        characterManager = new CharacterManager();
        recordingHandler = new RecordingHandler();
        playbackHandler = new PlaybackHandler();
        playerSession = new PlayerSession();
        narrativeCraftLogoRenderer = new NarrativeCraftLogoRenderer(ResourceLocation.withDefaultNamespace("textures/narrativecraft_logo.png"));
        narrativeUserOptions = new NarrativeUserOptions();
        isCutsceneMode = false;
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

    public RecordingHandler getRecordingHandler() {
        return recordingHandler;
    }

    public PlaybackHandler getPlaybackHandler() {
        return playbackHandler;
    }

    public boolean isCutsceneMode() {
        return isCutsceneMode;
    }

    public void setCutsceneMode(boolean cutsceneMode) {
        isCutsceneMode = cutsceneMode;
    }

    public StoryHandler getStoryHandler() {
        return storyHandler;
    }

    public void setStoryHandler(StoryHandler storyHandler) {
        this.storyHandler = storyHandler;
    }

    public Dialog getTestDialog() {
        return testDialog;
    }

    public void setTestDialog(Dialog testDialog) {
        this.testDialog = testDialog;
    }

    public Dialog2d getTestDialog2d() {
        return testDialog2d;
    }

    public void setTestDialog2d(Dialog2d testDialog2d) {
        this.testDialog2d = testDialog2d;
    }

    public PlayerSession getPlayerSession() {
        return playerSession;
    }

    public NarrativeUserOptions getNarrativeUserOptions() {
        return narrativeUserOptions;
    }

    public void setNarrativeUserOptions(NarrativeUserOptions narrativeUserOptions) {
        this.narrativeUserOptions = narrativeUserOptions;
    }

    public NarrativeCraftLogoRenderer getNarrativeCraftLogoRenderer() {
        return narrativeCraftLogoRenderer;
    }
}
