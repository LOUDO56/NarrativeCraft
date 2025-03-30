package fr.loudo.narrativecraft.narrative.scenes;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the scenes within the narrative.
 */
public class SceneManager {

    private List<Scene> scenes;

    /**
     * Initializes the SceneManager with an empty list of scenes.
     */
    public SceneManager() {
        this.scenes = new ArrayList<>();
    }

    /**
     * Provides suggestions for scenes based on the chapter index.
     *
     * @return a SuggestionProvider for scenes related to a chapter.
     */
    public SuggestionProvider<CommandSourceStack> getSceneSuggestionsByChapter() {
        return (context, builder) -> {
            int chapterIndex = IntegerArgumentType.getInteger(context, "chapter_index");
            for (Scene scene : scenes) {
                if (scene.getChapter().getIndex() == chapterIndex) {
                    builder.suggest(scene.getName());
                }
            }
            return builder.buildFuture();
        };
    }

    /**
     * Checks if a scene exists by its name.
     *
     * @param sceneName the name of the scene to check.
     * @return true if the scene exists, false otherwise.
     */
    public boolean sceneExists(String sceneName) {
        for (Scene scene : scenes) {
            if (scene.getName().equalsIgnoreCase(sceneName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves a scene by its name.
     *
     * @param sceneName the name of the scene to retrieve.
     * @return the Scene object if found, null otherwise.
     */
    public Scene getSceneByName(String sceneName) {
        for (Scene scene : scenes) {
            if (scene.getName().equals(sceneName)) {
                return scene;
            }
        }
        return null;
    }

    /**
     * Adds a new scene to the list of scenes if it does not already exist.
     *
     * @param scene the new scene to add.
     */
    public void addScene(Scene scene) {
        if (!scenes.contains(scene)) {
            scenes.add(scene);
        }
    }

    /**
     * Removes a scene from the list of scenes.
     *
     * @param scene the scene to remove.
     */
    public void removeScene(Scene scene) {
        scenes.remove(scene);
    }
}

