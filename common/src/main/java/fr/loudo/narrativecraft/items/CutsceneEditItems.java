package fr.loudo.narrativecraft.items;

import com.mojang.authlib.properties.Property;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CutsceneEditItems {

    private static final Property NEXT_SECOND_TEXTURE = new Property("textures", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTU2YTM2MTg0NTllNDNiMjg3YjIyYjdlMjM1ZWM2OTk1OTQ1NDZjNmZjZDZkYzg0YmZjYTRjZjMwYWI5MzExIn19fQ==");
    private static final Property PREVIOUS_SECOND_TEXTURE = new Property("textures", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2E2Y2E0NWMxYjAyOTQxMDk3NWNjMGI3NjEyMTZjNGEwNTRlNzFhMWQxMjg1MWY5NDA0MjgxYTk2YTM0N2I3OSJ9fX0=");
    private static final Property CAMERA_TEXTURE = new Property("textures", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTg2OWRiODU4M2I4NjdmODRhMjc3YTliNGY5MDE3ZmM1ZTIyNzQ0MTMzMzkxZjcwZDQ1M2I2NzljMzIzZjljZCJ9fX0=");

    public static ItemStack createKeyframeGroup;
    public static ItemStack addKeyframe;
    public static ItemStack cutscenePlaying;
    public static ItemStack cutscenePause;
    public static ItemStack nextSecond;
    public static ItemStack previousSecond;
    public static ItemStack camera;
    public static ItemStack settings;

    public static void init(RegistryAccess access) {
        createKeyframeGroup = getItem(Translation.message("items.cutscene.keyframegroup.name").getString(), access, Items.BOOK);
        addKeyframe = getItem(Translation.message("items.cutscene.keyframe.name").getString(), access, Items.REDSTONE_TORCH);
        cutscenePlaying = getItem(Translation.message("items.cutscene.playing.name").getString(), access, Items.LIME_DYE);
        cutscenePause = getItem(Translation.message("items.cutscene.paused.name").getString(), access, Items.GRAY_DYE);
        camera = getItemWithTexture("", access, Items.PLAYER_HEAD, CAMERA_TEXTURE);
        settings = getItem(Translation.message("items.cutscene.settings.name").getString(), access, Items.NETHER_STAR);
    }

    public static void initSkipItems(RegistryAccess access, int secondSkip) {
        nextSecond = getItemWithTexture(Translation.message("items.cutscene.skip.plus.name", secondSkip).getString(), access, Items.PLAYER_HEAD, NEXT_SECOND_TEXTURE);
        previousSecond = getItemWithTexture(Translation.message("items.cutscene.skip.minus.name", secondSkip).getString(), access, Items.PLAYER_HEAD, PREVIOUS_SECOND_TEXTURE);
    }

    private static ItemStack getItem(String name, RegistryAccess registryAccess, Item item) {

        CompoundTag tag = Utils.tagFromIdAndComponents(item, "{\"minecraft:custom_name\":\"" + name + "\"}");

        return ItemStack.parse(registryAccess, tag).get();

    }

    private static ItemStack getItemWithTexture(String name, RegistryAccess registryAccess, Item item, Property textures) {

        CompoundTag tag = Utils.tagFromIdAndComponents(item, "{\"minecraft:custom_name\":\"" + name + "\", \"minecraft:profile\":{properties:[{name: \"" + textures.name() + "\", value: \"" + textures.value() + "\"}]}}");

        return ItemStack.parse(registryAccess, tag).get();

    }
}
