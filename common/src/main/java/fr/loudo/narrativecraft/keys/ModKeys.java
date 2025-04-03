package fr.loudo.narrativecraft.keys;

import com.mojang.blaze3d.platform.InputConstants;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraft.client.KeyMapping;

public class ModKeys {

    public static final KeyMapping START_ANIMATION_RECORDING = registerKey("key.animation.record.start", InputConstants.KEY_V);
    public static final KeyMapping STOP_ANIMATION_RECORDING = registerKey("key.animation.record.stop", InputConstants.KEY_B);

    private static KeyMapping registerKey(String translationKey, int code) {
        return new KeyMapping(
                translationKey,
                InputConstants.Type.KEYSYM,
                code,
                "key.categories." + NarrativeCraftMod.MOD_ID
        );
    }

}
