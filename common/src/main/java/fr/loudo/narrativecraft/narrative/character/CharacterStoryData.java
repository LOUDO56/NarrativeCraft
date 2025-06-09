package fr.loudo.narrativecraft.narrative.character;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.utils.FakePlayer;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CharacterStoryData {

    private final CharacterStory characterStory;
    private final List<ItemSlotData> itemSlotDataList;
    private double x, y, z;
    private float pitch, yaw;
    private float yBodyRot;
    private String pose;

    public CharacterStoryData(CharacterStory characterStory) {
        this.characterStory = characterStory;
        itemSlotDataList = new ArrayList<>();
        init();
    }

    private void init() {
        LivingEntity livingEntity = characterStory.getEntity();
        if(livingEntity == null) return;
        x = livingEntity.getX();
        y = livingEntity.getY();
        z = livingEntity.getZ();
        pitch = livingEntity.getXRot();
        yaw = livingEntity.getYRot();
        yBodyRot = livingEntity.yBodyRot;
        pose = livingEntity.getPose().name();
        for(EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack itemStack = livingEntity.getItemBySlot(equipmentSlot);
            if(!itemStack.isEmpty()) {
                Tag tag = itemStack.save(livingEntity.registryAccess());
                Tag componentsTag = ((CompoundTag)tag).get("components");
                String itemData = componentsTag == null ? "" : componentsTag.toString();
                itemSlotDataList.add(
                        new ItemSlotData(
                                BuiltInRegistries.ITEM.getId(itemStack.getItem()),
                                itemData,
                                equipmentSlot.name()
                        )
                );
            }
        }
    }

    public CharacterStory getCharacterStory() {
        return characterStory;
    }

    public void spawn(ServerLevel serverLevel) {
        LivingEntity livingEntity = new FakePlayer(serverLevel, new GameProfile(UUID.randomUUID(), characterStory.getName()));
        livingEntity.snapTo(x, y, z);
        livingEntity.setXRot(pitch);
        livingEntity.setYRot(yaw);
        livingEntity.setYHeadRot(yaw);
        livingEntity.setYBodyRot(yBodyRot);
        livingEntity.setPose(Pose.valueOf(pose));
        for(ItemSlotData itemSlotData : itemSlotDataList) {
            livingEntity.setItemSlot(EquipmentSlot.valueOf(itemSlotData.equipmentSlot), itemSlotData.getItem(livingEntity.registryAccess()));
            serverLevel.getServer().getPlayerList().broadcastAll(new ClientboundSetEquipmentPacket(
                    livingEntity.getId(),
                    List.of(new Pair<>(EquipmentSlot.valueOf(itemSlotData.equipmentSlot), itemSlotData.getItem(livingEntity.registryAccess())))
            ));
        }

        if(livingEntity instanceof FakePlayer fakePlayer) {
            serverLevel.getServer().getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, fakePlayer));
        }
        serverLevel.addFreshEntity(livingEntity);
        characterStory.setEntity(livingEntity);


    }

    private record ItemSlotData(int id, String data, String equipmentSlot) {
        public ItemStack getItem(RegistryAccess registryAccess) {
                Item item = BuiltInRegistries.ITEM.byId(id);
                ItemStack itemStack = new ItemStack(item);
                CompoundTag tag = Utils.tagFromIdAndComponents(item, data);
                if (tag != null) {
                    itemStack = ItemStack.parse(registryAccess, tag).orElse(ItemStack.EMPTY);
                }
                return itemStack;
            }
        }
}
