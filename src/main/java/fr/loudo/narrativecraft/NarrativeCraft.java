package fr.loudo.narrativecraft;

import fr.loudo.narrativecraft.narrative.scenes.SceneManager;
import fr.loudo.narrativecraft.narrative.chapter.ChapterManager;
import fr.loudo.narrativecraft.narrative.character.CharacterManager;
import net.minecraftforge.fml.common.Mod;

@Mod(NarrativeCraft.MODID)
public class NarrativeCraft {

    public static final String MODID = "narrativecraft";

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
