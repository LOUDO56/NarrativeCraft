package fr.loudo.narrativecraft.items;

import com.mojang.authlib.properties.Property;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CutsceneEditItems {

    private static final Property CAMERA_TEXTURE = new Property("textures", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTg2OWRiODU4M2I4NjdmODRhMjc3YTliNGY5MDE3ZmM1ZTIyNzQ0MTMzMzkxZjcwZDQ1M2I2NzljMzIzZjljZCJ9fX0=");
    private static final Property TRIGGER_TEXTURE = new Property("textures", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzQ2NWMxMjE5NThjMDUyMmUzZGNjYjNkMTRkNjg2MTJkNjMxN2NkMzgwYjBlNjQ2YjYxYjc0MjBiOTA0YWYwMiJ9fX0=");
    public static ItemStack camera;
    public static ItemStack trigger;

    public static void init(RegistryAccess access) {
        camera = getItemWithTexture("", access, Items.PLAYER_HEAD, CAMERA_TEXTURE);
        trigger = getItemWithTexture("", access, Items.PLAYER_HEAD, TRIGGER_TEXTURE);
    }

    private static ItemStack getItem(String name, RegistryAccess registryAccess, Item item) {

        CompoundTag tag = Utils.tagFromIdAndComponents(item, "{\"minecraft:custom_name\":\"" + name + "\"}");

        return Utils.generateItemStackFromNBT(tag, registryAccess);

    }

    private static ItemStack getItemWithTexture(String name, RegistryAccess registryAccess, Item item, Property textures) {

        CompoundTag tag = Utils.tagFromIdAndComponents(item, "{\"minecraft:custom_name\":\"" + name + "\", \"minecraft:profile\":{properties:[{name: \"" + textures.name() + "\", value: \"" + textures.value() + "\"}]}}");

        return Utils.generateItemStackFromNBT(tag, registryAccess);

    }

}
