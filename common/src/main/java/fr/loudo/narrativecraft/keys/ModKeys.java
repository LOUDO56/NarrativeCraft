package fr.loudo.narrativecraft.keys;

import com.mojang.blaze3d.platform.InputConstants;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraft.client.KeyMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModKeys {

    private static final Map<KeyMapping, Boolean> previousStatesKeyMapping = new HashMap<>();
    private static final Map<Integer, Boolean> previousStatesKeyCode = new HashMap<>();
    private static final List<KeyMapping> ALL_KEYS = new ArrayList<>();

    public static final KeyMapping OPEN_STORY_MANAGER = registerKey("key.screen.story.open", InputConstants.KEY_N);
    public static final KeyMapping START_ANIMATION_RECORDING = registerKey("key.animation.record.start", InputConstants.KEY_V);
    public static final KeyMapping STOP_ANIMATION_RECORDING = registerKey("key.animation.record.stop", InputConstants.KEY_B);
    public static final KeyMapping OPEN_KEYFRAME_EDIT_SCREEN = registerKey("key.screen.keyframe_controller", InputConstants.KEY_G);
    public static final KeyMapping NEXT_DIALOG = registerKey("key.dialog.next", InputConstants.KEY_RETURN);
    public static final KeyMapping SELECT_CHOICE_1 = registerKey("key.choice.1", InputConstants.KEY_1);
    public static final KeyMapping SELECT_CHOICE_2 = registerKey("key.choice.2", InputConstants.KEY_2);
    public static final KeyMapping SELECT_CHOICE_3 = registerKey("key.choice.3", InputConstants.KEY_3);
    public static final KeyMapping SELECT_CHOICE_4 = registerKey("key.choice.4", InputConstants.KEY_4);

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
        boolean wasDown = previousStatesKeyMapping.getOrDefault(key, false);

        if (isDown && !wasDown) {
            action.run();
        }

        previousStatesKeyMapping.put(key, isDown);
    }

    public static void handleKeyPress(int code, boolean isDown, Runnable action) {
        boolean wasDown = previousStatesKeyCode.getOrDefault(code, false);

        if (isDown && !wasDown) {
            action.run();
        }

        previousStatesKeyCode.put(code, isDown);
    }

}

