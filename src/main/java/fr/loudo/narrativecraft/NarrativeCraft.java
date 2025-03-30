package fr.loudo.narrativecraft;

import com.bladecoder.ink.runtime.Story;
import fr.loudo.narrativecraft.scenes.SceneManager;
import fr.loudo.narrativecraft.story.ChapterManager;
import net.minecraftforge.fml.common.Mod;

@Mod(NarrativeCraft.MODID)
public class NarrativeCraft {

    public static final String MODID = "narrativecraft";

    private static final ChapterManager CHAPTER_MANAGER = new ChapterManager();
    private static final SceneManager SCENE_MANAGER = new SceneManager();

    public static ChapterManager getChapterManager() {
        return CHAPTER_MANAGER;
    }

    public static SceneManager getSceneManager() {
        return SCENE_MANAGER;
    }



}
