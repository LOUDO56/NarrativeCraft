package fr.loudo.narrativecraft.utils;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DynamicOps;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;

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
        return TagParser.parseCompoundAsArgument(new StringReader(nbtString));
    }

    public static ValueInput valueInputFromCompoundTag(RegistryAccess registryAccess, String nbtString) throws CommandSyntaxException {
        return TagValueInput.create(ProblemReporter.DISCARDING, registryAccess, nbtFromString(nbtString));
    }

    public static Tag getItemTag(ItemStack itemStack, RegistryAccess registryAccess) {
        DynamicOps<Tag> ops = registryAccess.createSerializationContext(NbtOps.INSTANCE);
        Tag tag;
        try { tag = ItemStack.CODEC.encodeStart(ops, itemStack).getOrThrow(); }
        catch (Exception exception) { tag = new CompoundTag(); }
        return tag;
    }

    public static ItemStack generateItemStackFromNBT(CompoundTag compoundTag, RegistryAccess registryAccess) {
        DynamicOps<Tag> ops = registryAccess.createSerializationContext(NbtOps.INSTANCE);
        if (compoundTag == null) { return ItemStack.EMPTY; }
        try { return ItemStack.CODEC.parse(ops, compoundTag).getOrThrow(); }
        catch (Exception e) { return ItemStack.EMPTY; }
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
        return NarrativeCraftMod.server.getPlayerList().getPlayer(Minecraft.getInstance().player.getUUID()).level();
    }

    public static String getSnakeCase(String text) {
        return String.join("_", text.toLowerCase().split(" "));
    }
    
    public static void disconnectPlayer(Minecraft minecraft) {
        PauseScreen.disconnectFromWorld(minecraft, ClientLevel.DEFAULT_QUIT_MESSAGE);
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

    public static Matrix4f convert(Matrix3x2f mat) {
        Matrix4f result = new Matrix4f();
        result.m00(mat.m00());
        result.m01(mat.m01());
        result.m10(mat.m10());
        result.m11(mat.m11());
        result.m30(mat.m20());
        result.m31(mat.m21());
        return result;
    }

}
