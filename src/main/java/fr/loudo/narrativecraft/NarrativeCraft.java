package fr.loudo.narrativecraft;

import fr.loudo.narrativecraft.narrative.chapter.ChapterManager;
import fr.loudo.narrativecraft.narrative.character.CharacterManager;
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

    public NarrativeCraft() {
        chapterManager = new ChapterManager();
        characterManager = new CharacterManager();
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



}
