package fr.loudo.narrativecraft;

import fr.loudo.narrativecraft.narrative.chapter.ChapterManager;
import fr.loudo.narrativecraft.narrative.character.CharacterManager;
import fr.loudo.narrativecraft.narrative.scenes.SceneManager;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(NarrativeCraft.MODID)
public class NarrativeCraft {

    public static final String MODID = "narrativecraft";
    public static final Logger LOGGER = LogManager.getLogger();

    private static final ChapterManager CHAPTER_MANAGER = new ChapterManager();
    private static final SceneManager SCENE_MANAGER = new SceneManager();
    private static final CharacterManager CHARACTER_MANAGER = new CharacterManager();


    public static ChapterManager getChapterManager() {
        return CHAPTER_MANAGER;
    }

    public static SceneManager getSceneManager() {
        return SCENE_MANAGER;
    }

    public static CharacterManager getCharacterManager() {
        return CHARACTER_MANAGER;
    }



}
