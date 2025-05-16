package fr.loudo.narrativecraft.keys;

import com.mojang.blaze3d.platform.InputConstants;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraft.client.KeyMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModKeys {

    private static final Map<KeyMapping, Boolean> previousStates = new HashMap<>();
    private static final List<KeyMapping> ALL_KEYS = new ArrayList<>();

    public static final KeyMapping OPEN_STORY_MANAGER = registerKey("key.screen.story.open", InputConstants.KEY_N);
    public static final KeyMapping START_ANIMATION_RECORDING = registerKey("key.animation.record.start", InputConstants.KEY_V);
    public static final KeyMapping STOP_ANIMATION_RECORDING = registerKey("key.animation.record.stop", InputConstants.KEY_B);
    public static final KeyMapping SCREEN_KEYFRAME_OPTION = registerKey("key.cutscene.screen.keyframe_option", InputConstants.KEY_Y);
    public static final KeyMapping CREATE_KEYFRAME_GROUP = registerKey("key.cutscene.keyframes.create_group", InputConstants.KEY_J);
    public static final KeyMapping ADD_KEYFRAME = registerKey("key.cutscene.keyframes.add_keyframe", InputConstants.KEY_K);
    public static final KeyMapping OPEN_KEYFRAME_EDIT_SCREEN = registerKey("key.cutscene.keyframes.cutscene_controller_screen", InputConstants.KEY_F);


    private static KeyMapping registerKey(String translationKey, int code) {
        KeyMapping key = new KeyMapping(
                translationKey,
                InputConstants.Type.KEYSYM,
                code,
                "key.categories." + NarrativeCraftMod.MOD_ID
        );
        ALL_KEYS.add(key);
        return key;
    }

    public static List<KeyMapping> getAllKeys() {
        return ALL_KEYS;
    }

    public static void handleKeyPress(KeyMapping key, Runnable action) {
        boolean isDown = key.isDown();
        boolean wasDown = previousStates.getOrDefault(key, false);

        if (isDown && !wasDown) {
            action.run();
        }

        previousStates.put(key, isDown);
    }
}

