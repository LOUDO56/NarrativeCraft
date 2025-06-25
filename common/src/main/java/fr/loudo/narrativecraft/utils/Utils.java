package fr.loudo.narrativecraft.utils;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

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

    public static ServerPlayer getServerPlayerByUUID(UUID uuid) {
        if(NarrativeCraftMod.server == null) return null;
        return NarrativeCraftMod.server.getPlayerList().getPlayer(uuid);
    }

    public static ServerLevel getServerLevel() {
        return NarrativeCraftMod.server.getPlayerList().getPlayer(Minecraft.getInstance().player.getUUID()).serverLevel();
    }

    public static PlayerSession getSessionOrNull(ServerPlayer player) {
        PlayerSession playerSession = NarrativeCraftMod.getInstance().getPlayerSessionManager().getPlayerSession(player);
        if(playerSession == null) return null;
        if(playerSession.getChapter() == null || playerSession.getScene() == null) {
            return null;
        }
        return playerSession;
    }

    public static PlayerSession getSessionOrNull(UUID uuid) {
        if(NarrativeCraftMod.server == null) return null;
        ServerPlayer serverPlayer = NarrativeCraftMod.server.getPlayerList().getPlayer(uuid);
        PlayerSession playerSession = NarrativeCraftMod.getInstance().getPlayerSessionManager().getPlayerSession(serverPlayer);
        if(playerSession == null) return null;
        if(playerSession.getChapter() == null || playerSession.getScene() == null) {
            return null;
        }
        return playerSession;
    }

    public static String getSnakeCase(String text) {
        return String.join("_", text.toLowerCase().split(" "));
    }
}
