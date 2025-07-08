package fr.loudo.narrativecraft.narrative.character;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.mixin.fields.EntityFields;
import fr.loudo.narrativecraft.mixin.fields.LivingEntityFields;
import fr.loudo.narrativecraft.mixin.fields.PlayerFields;
import fr.loudo.narrativecraft.mixin.fields.PlayerListFields;
import fr.loudo.narrativecraft.utils.FakePlayer;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CharacterStoryData {

    private CharacterStory characterStory;
    private double x, y, z;
    private float pitch, yaw;
    private float yBodyRot;
    private String pose;
    private byte entityByte;
    private byte livingEntityByte;
    private String skinName;
    private final List<ItemSlotData> itemSlotDataList;
    private boolean onlyTemplate;

    public CharacterStoryData(CharacterStory characterStory) {
        this.characterStory = characterStory;
        skinName = characterStory.getCharacterSkinController().getCurrentSkin().getName();
        itemSlotDataList = new ArrayList<>();
        LivingEntity livingEntity = characterStory.getEntity();
        if(livingEntity == null) return;
        x = livingEntity.getX();
        y = livingEntity.getY();
        z = livingEntity.getZ();
        pitch = livingEntity.getXRot();
        yaw = livingEntity.getYRot();
        yBodyRot = livingEntity.yBodyRot;
        pose = livingEntity.getPose().name();
        entityByte = livingEntity.getEntityData().get(EntityFields.getDATA_SHARED_FLAGS_ID());
        livingEntityByte = livingEntity.getEntityData().get(LivingEntityFields.getDATA_LIVING_ENTITY_FLAGS());
        onlyTemplate = false;
        initItem(livingEntity);
    }

    public CharacterStoryData(CharacterStory characterStory, String skinName, double x, double y, double z, float XRot, float YRot, boolean onlyTemplate) {
        this.characterStory = characterStory;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = XRot;
        this.yaw = YRot;
        this.skinName = skinName;
        itemSlotDataList = new ArrayList<>();
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        pose = localPlayer.getPose().name();
        entityByte = localPlayer.getEntityData().get(EntityFields.getDATA_SHARED_FLAGS_ID());
        livingEntityByte = localPlayer.getEntityData().get(LivingEntityFields.getDATA_LIVING_ENTITY_FLAGS());
        this.onlyTemplate = onlyTemplate;
        if(!onlyTemplate) {
            initItem(localPlayer);
        }
    }

    public CharacterStoryData(CharacterStory characterStory, double x, double y, double z, float pitch, float yaw, float yBodyRot, String pose, byte entityByte, byte livingEntityByte, String skinName, List<ItemSlotData> itemSlotDataList, boolean onlyTemplate) {
        this.characterStory = characterStory;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
        this.yBodyRot = yBodyRot;
        this.pose = pose;
        this.entityByte = entityByte;
        this.livingEntityByte = livingEntityByte;
        this.skinName = skinName;
        this.itemSlotDataList = itemSlotDataList;
        this.onlyTemplate = onlyTemplate;
    }

    public void initItem(LivingEntity entity) {
        itemSlotDataList.clear();
        for(EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack itemStack = entity.getItemBySlot(equipmentSlot);
            if(!itemStack.isEmpty()) {
                Tag tag = itemStack.save(entity.registryAccess());
                Tag componentsTag = ((CompoundTag)tag).get("components");
                String itemData = componentsTag == null ? "" : componentsTag.toString();
                itemSlotDataList.add(
                        new ItemSlotData(
                                BuiltInRegistries.ITEM.getId(itemStack.getItem()),
                                itemData,
                                equipmentSlot.name()
                        )
                );
            } else {
                itemSlotDataList.add(
                        new ItemSlotData(
                                BuiltInRegistries.ITEM.getId(Items.AIR),
                                "",
                                equipmentSlot.name()
                        )
                );
            }
        }
    }

    public void spawn(ServerLevel serverLevel) {
        LivingEntity livingEntity;
        if(BuiltInRegistries.ENTITY_TYPE.getId(characterStory.getEntityType()) == BuiltInRegistries.ENTITY_TYPE.getId(EntityType.PLAYER)) {
            livingEntity = new FakePlayer(serverLevel, new GameProfile(UUID.randomUUID(), characterStory.getName()));
        } else {
            livingEntity = (LivingEntity) characterStory.getEntityType().create(serverLevel, EntitySpawnReason.MOB_SUMMONED);
            if(livingEntity instanceof Mob mob) {
                mob.setNoAi(true);
                mob.setSilent(true);
                mob.setInvulnerable(true);
            }
        }
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

        SynchedEntityData entityData = livingEntity.getEntityData();
        if(livingEntity instanceof FakePlayer fakePlayer) {
            serverLevel.getServer().getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, fakePlayer));
            fakePlayer.getEntityData().set(PlayerFields.getDATA_PLAYER_MODE_CUSTOMISATION(), (byte) 0b01111111);
        }
        entityData.set(EntityFields.getDATA_SHARED_FLAGS_ID(), entityByte);
        entityData.set(LivingEntityFields.getDATA_LIVING_ENTITY_FLAGS(), livingEntityByte);
        livingEntity.setInvisible(false);
        if(livingEntity instanceof FakePlayer fakePlayer) {
            serverLevel.getServer().getPlayerList().getPlayers().add(fakePlayer);
            ((PlayerListFields)serverLevel.getServer().getPlayerList()).getPlayersByUUID().put(fakePlayer.getUUID(), fakePlayer);
            serverLevel.getServer().getPlayerList().broadcastAll(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(fakePlayer)));
            serverLevel.addNewPlayer(fakePlayer);
        } else {
            serverLevel.addFreshEntity(livingEntity);
        }
        if(characterStory.getCharacterType() == CharacterStory.CharacterType.MAIN) {
            characterStory = NarrativeCraftMod.getInstance().getCharacterManager().getCharacter(characterStory.getName());
        }
        characterStory.getCharacterSkinController().setCurrentSkin(characterStory.getCharacterSkinController().getSkinFile(skinName));
        characterStory.setEntity(livingEntity);
    }

    public String getSkinName() {
        return skinName;
    }

    public void setSkinName(String skinName) {
        this.skinName = skinName;
    }

    public CharacterStory getCharacterStory() {
        return characterStory;
    }

    public void setCharacterStory(CharacterStory characterStory) {
        this.characterStory = characterStory;
    }

    public List<ItemSlotData> getItemSlotDataList() {
        return itemSlotDataList;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public byte getEntityByte() {
        return entityByte;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public float getyBodyRot() {
        return yBodyRot;
    }

    public Pose getPose() {
        return Pose.valueOf(pose);
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void setEntityByte(byte entityByte) {
        this.entityByte = entityByte;
    }

    public byte getLivingEntityByte() {
        return livingEntityByte;
    }

    public void setLivingEntityByte(byte livingEntityByte) {
        this.livingEntityByte = livingEntityByte;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setyBodyRot(float yBodyRot) {
        this.yBodyRot = yBodyRot;
    }

    public void setPose(Pose pose) {
        this.pose = pose.name();
    }

    public boolean isOnlyTemplate() {
        return onlyTemplate;
    }

    public void setOnlyTemplate(boolean onlyTemplate) {
        this.onlyTemplate = onlyTemplate;
    }

    public record ItemSlotData(int id, String data, String equipmentSlot) {
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
