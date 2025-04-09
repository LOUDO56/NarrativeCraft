package fr.loudo.narrativecraft.items;

import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ModItems {

    public static ItemStack cutscenePlaying;
    public static ItemStack cutscenePause;

    public static void init(RegistryAccess access) {
        cutscenePlaying = getItem(Translation.message("items.cutscene.playing.name").getString(), access, Items.LIME_DYE);
        cutscenePause = getItem(Translation.message("items.cutscene.paused.name").getString(), access, Items.GRAY_DYE);
    }

    private static ItemStack getItem(String name, RegistryAccess registryAccess, Item item) {

        CompoundTag tag = Utils.tagFromIdAndComponents(item, "{\"minecraft:custom_name\":\"" + name + "\"}");

        return ItemStack.parse(registryAccess, tag).get();

    }
}
