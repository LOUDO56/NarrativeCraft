package fr.loudo.narrativecraft.registers;

import fr.loudo.narrativecraft.keys.ModKeys;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

public class ModKeysRegister {

    public static void register() {
        for (KeyMapping key : ModKeys.getAllKeys()) {
            KeyBindingHelper.registerKeyBinding(key);
        }
    }

}
