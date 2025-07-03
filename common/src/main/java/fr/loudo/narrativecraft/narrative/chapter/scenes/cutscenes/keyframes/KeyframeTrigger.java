package fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes;

import com.mojang.datafixers.util.Pair;
import fr.loudo.narrativecraft.items.CutsceneEditItems;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.Arrays;
import java.util.List;

public class KeyframeTrigger extends Keyframe {

    private String commands;

    public KeyframeTrigger(int id, KeyframeCoordinate keyframeCoordinate, int tick, String commands) {
        super(id, keyframeCoordinate, tick, 0, 0);
        this.commands = commands;
    }

    public void setCommands(String commands) {
        this.commands = commands;
    }

    public String getCommands() {
        return commands;
    }

    @Override
    public void showKeyframeToClient(ServerPlayer player) {
        super.showKeyframeToClient(player);
        player.connection.send(new ClientboundSetEquipmentPacket(
                cameraEntity.getId(),
                List.of(new Pair<>(EquipmentSlot.HEAD, CutsceneEditItems.trigger))
        ));
        cameraEntity.setCustomNameVisible(true);
        cameraEntity.setCustomName(Component.literal("Trigger"));
        updateEntityData(player);
    }

    public List<String> getCommandsToList() {
        return Arrays.stream(commands.split("\n")).toList();
    }
}
