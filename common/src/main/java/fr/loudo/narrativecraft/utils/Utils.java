package fr.loudo.narrativecraft.utils;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.realmsclient.RealmsMainScreen;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

public class Utils {

    public static final String REGEX_FLOAT = "^(-|[+-]?([0-9]+([.,][0-9]*)?|[.,][0-9]+))?$";
    public static final String REGEX_FLOAT_POSITIVE_ONLY = "^([+]?([0-9]+([.,][0-9]*)?|[.,][0-9]+))?$";

    // https://github.com/mt1006/mc-mocap-mod/blob/1.21.1/common/src/main/java/net/mt1006/mocap/mocap/actions/ChangeItem.java#L291
    public static CompoundTag tagFromIdAndComponents(Item item, String data)
    {
        CompoundTag tag = new CompoundTag();

        try { tag.put("components", nbtFromString(data)); }
        catch (CommandSyntaxException e) { return null; }

        tag.put("id", StringTag.valueOf(BuiltInRegistries.ITEM.getKey(item).toString()));
        tag.put("count", IntTag.valueOf(1));
        return tag;
    }

    // https://github.com/mt1006/mc-mocap-mod/blob/1.21.1/common/src/main/java/net/mt1006/mocap/utils/Utils.java#L61
    public static CompoundTag nbtFromString(String nbtString) throws CommandSyntaxException
    {
        return TagParser.parseTag(nbtString);
    }

    public static BlockState getBlockStateFromData(String data, RegistryAccess registry) {
        try {
            CompoundTag compoundTag = Utils.nbtFromString(data);
            return NbtUtils.readBlockState(registry.lookupOrThrow(Registries.BLOCK), compoundTag);
        } catch (CommandSyntaxException ignored) {
            return null;
        }
    }

    public static ServerPlayer getServerPlayerByUUID(UUID uuid) {
        if(NarrativeCraftMod.server == null) return null;
        return NarrativeCraftMod.server.getPlayerList().getPlayer(uuid);
    }

    public static ServerLevel getServerLevel() {
        return NarrativeCraftMod.server.getPlayerList().getPlayer(Minecraft.getInstance().player.getUUID()).serverLevel();
    }

    public static String getSnakeCase(String text) {
        return String.join("_", text.toLowerCase().split(" "));
    }
    
    public static void disconnectPlayer(Minecraft minecraft) {
        boolean flag = minecraft.isLocalServer();
        ServerData serverdata = minecraft.getCurrentServer();
        minecraft.level.disconnect();
        if (flag) {
            minecraft.disconnect(new GenericMessageScreen(Component.translatable("menu.returnToMenu")));
        } else {
            minecraft.disconnect();
        }

        TitleScreen titlescreen = new TitleScreen();
        if (flag) {
            minecraft.setScreen(titlescreen);
        } else if (serverdata != null && serverdata.isRealm()) {
            minecraft.setScreen(new RealmsMainScreen(titlescreen));
        } else {
            minecraft.setScreen(new JoinMultiplayerScreen(titlescreen));
        }
    }

    public static int[] getImageResolution(ResourceLocation resourceLocation) {
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();

        Optional<Resource> resource = resourceManager.getResource(resourceLocation);
        if (resource.isPresent()) {
            try (InputStream stream = resource.get().open()) {
                BufferedImage image = ImageIO.read(stream);
                int width = image.getWidth();
                int height = image.getHeight();

                return new int[]{width, height};
            } catch (IOException ignored) {}
        }
        return null;
    }

    public static int getDynamicHeight(int[] resolution, int newWidth) {
        float ratio = (float) resolution[1] / resolution[0];
        return Math.round(ratio * newWidth);
    }

    public static boolean resourceExists(ResourceLocation resourceLocation) {
        return Minecraft.getInstance().getResourceManager().getResource(resourceLocation).isPresent();
    }
}
