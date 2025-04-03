package fr.loudo.narrativecraft.narrative.recordings.actions;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;


public class ItemChangeAction extends Action {

    private int itemId;
    private String data;
    private String equipmentSlot;

    public ItemChangeAction(int waitTick, ActionType actionType, String equipmentSlot, int itemId) {
        super(waitTick, actionType);
        this.itemId = itemId;
        this.equipmentSlot = equipmentSlot;
        this.data = "";
    }

    public ItemChangeAction(int waitTick, ActionType actionType, int itemId, String equipmentSlot, String data) {
        super(waitTick, actionType);
        this.itemId = itemId;
        this.equipmentSlot = equipmentSlot;
        this.data = data;
    }

    @Override
    public void execute(LivingEntity entity) {
        Item item = Item.byId(itemId);
        ItemStack itemStack = new ItemStack(item);
        CompoundTag tag = tagFromIdAndComponents(item);
        if (tag != null) {
            itemStack = ItemStack.parse(entity.registryAccess(), tag).orElse(ItemStack.EMPTY);
        }

        entity.getServer().getPlayerList().broadcastAll(new ClientboundSetEquipmentPacket(
                entity.getId(),
                List.of(new Pair<>(EquipmentSlot.valueOf(equipmentSlot), itemStack))
        ));
    }

    // https://github.com/mt1006/mc-mocap-mod/blob/1.21.1/common/src/main/java/net/mt1006/mocap/mocap/actions/ChangeItem.java#L291
    private CompoundTag tagFromIdAndComponents(Item item)
    {
        CompoundTag tag = new CompoundTag();

        try { tag.put("components", nbtFromString(data)); }
        catch (CommandSyntaxException e) { return null; }

        tag.put("id", StringTag.valueOf(BuiltInRegistries.ITEM.getKey(item).toString()));
        tag.put("count", IntTag.valueOf(1));
        return tag;
    }

    // https://github.com/mt1006/mc-mocap-mod/blob/1.21.1/common/src/main/java/net/mt1006/mocap/utils/Utils.java#L61
    private CompoundTag nbtFromString(String nbtString) throws CommandSyntaxException
    {
        return TagParser.parseCompoundAsArgument(new StringReader(nbtString));
    }

}
