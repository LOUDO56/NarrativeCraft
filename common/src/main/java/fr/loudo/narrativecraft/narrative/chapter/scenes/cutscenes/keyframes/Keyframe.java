package fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes;

import com.mojang.math.Transformation;
import fr.loudo.narrativecraft.mixin.fields.ArmorStandFields;
import fr.loudo.narrativecraft.mixin.fields.DisplayFields;
import fr.loudo.narrativecraft.mixin.fields.TextDisplayFields;
import fr.loudo.narrativecraft.utils.PlayerCoord;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Keyframe {

    private transient Display.ItemDisplay itemDisplay;
    private transient ArmorStand armorStand;
    private transient Display.TextDisplay textDisplay;
    private int id;
    private PlayerCoord position;
    private long startDelay;
    private long pathTime;

    public Keyframe(int id, PlayerCoord position, long startDelay, long pathTime) {
        this.id = id;
        this.position = position;
        this.startDelay = startDelay;
        this.pathTime = pathTime;
    }

    public void showKeyframeToClient(ServerPlayer player) {
        armorStand = new ArmorStand(EntityType.ARMOR_STAND, player.level());
        itemDisplay = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, player.level());
        itemDisplay.getSlot(0).set(new ItemStack(Items.ENDER_EYE));
        Transformation transformation = new Transformation(
                new Vector3f(0f, 0f, 0f),
                new Quaternionf(0f, 0f, 0f, 1f),
                new Vector3f(0.5f, 0.5f, 0.5f),
                new Quaternionf(0f, 0f, 0f, 1f)
        );
        player.level().addFreshEntity(itemDisplay);
        player.level().addFreshEntity(armorStand);
        updateItemPosition();
        ((ArmorStandFields)armorStand).callSetSmall(true);
        armorStand.setNoGravity(true);
        armorStand.setInvisible(true);
        armorStand.setNoBasePlate(true);
        ((DisplayFields)itemDisplay).callSetBillboardConstraints(Display.BillboardConstraints.CENTER);
        ((DisplayFields)itemDisplay).callSetTransformation(transformation);
        for(ServerPlayer serverPlayer : player.getServer().getPlayerList().getPlayers()) {
            if(serverPlayer.getId() != player.getId()) {
                serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(itemDisplay.getId()));
                serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(armorStand.getId()));
            }
        }
    }

    public void showStartGroupText(ServerPlayer player, int id) {
        textDisplay = new Display.TextDisplay(EntityType.TEXT_DISPLAY, player.level());
        Transformation transformation = new Transformation(
                new Vector3f(0f, 0f, 0f),
                new Quaternionf(0f, 0f, 0f, 1f),
                new Vector3f(0.5f, 0.5f, 0.5f),
                new Quaternionf(0f, 0f, 0f, 1f)
        );
        player.level().addFreshEntity(textDisplay);
        textDisplay.snapTo(position.getX(), position.getY() + 0.5, position.getZ());
        ((DisplayFields)textDisplay).callSetBillboardConstraints(Display.BillboardConstraints.CENTER);
        ((DisplayFields)textDisplay).callSetTransformation(transformation);
        ((TextDisplayFields)textDisplay).callSetText(Translation.message("cutscene.keyframegroup.text_display", id));
        ((TextDisplayFields)textDisplay).callSetBackgroundColor(16711680);
        for(ServerPlayer serverPlayer : player.getServer().getPlayerList().getPlayers()) {
            if(serverPlayer.getId() != player.getId()) {
                serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(textDisplay.getId()));
            }
        }
    }

    public void removeKeyframeFromClient() {
        itemDisplay.remove(Entity.RemovalReason.KILLED);
        armorStand.remove(Entity.RemovalReason.KILLED);
        if(textDisplay != null) {
            textDisplay.remove(Entity.RemovalReason.KILLED);
        }
    }

    public void updateItemPosition() {
        itemDisplay.snapTo(position.getX(), position.getY(), position.getZ());
        armorStand.snapTo(position.getX(), position.getY() - 0.5, position.getZ());
    }

    public PlayerCoord getPosition() {
        return position;
    }

    public void setPosition(PlayerCoord position) {
        this.position = position;
    }

    public long getStartDelay() {
        return startDelay;
    }


    public void setStartDelay(long startDelay) {
        this.startDelay = startDelay;
    }

    public long getPathTime() {
        return pathTime;
    }

    public void setPathTime(long pathTime) {
        this.pathTime = pathTime;
    }

    public ArmorStand getArmorStand() {
        return armorStand;
    }

    public int getId() {
        return id;
    }

}
