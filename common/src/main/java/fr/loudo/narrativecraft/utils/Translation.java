package fr.loudo.narrativecraft.utils;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Utility class for handling translation messages.
 */
public class Translation {

    /**
     * Retrieves a translatable message for the given path and arguments.
     *
     * @param path the translation key path.
     * @param args the arguments to be inserted into the translation string.
     * @return a MutableComponent containing the translatable message.
     */
    public static MutableComponent message(String path, Object... args) {
        return Component.translatable(NarrativeCraftMod.MOD_ID + "." + path, args);
    }

}

