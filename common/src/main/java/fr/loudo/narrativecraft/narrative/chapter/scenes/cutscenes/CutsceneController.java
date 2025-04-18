package fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes;

import fr.loudo.narrativecraft.items.CutsceneEditItems;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeGroup;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;
import fr.loudo.narrativecraft.screens.CutsceneSettingsScreen;
import fr.loudo.narrativecraft.utils.PlayerCoord;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CutsceneController {

    private transient final AtomicInteger keyframeGroupCounter = new AtomicInteger();
    private transient final AtomicInteger keyframeCounter = new AtomicInteger();

    private transient List<Entity> keyframesEntity;

    private Cutscene cutscene;
    private ServerPlayer player;
    private boolean isPlaying;
    private int currentTick;
    private int currentSkipCount;
    private KeyframeGroup selectedKeyframeGroup;

    public CutsceneController(Cutscene cutscene, ServerPlayer player) {
        this.cutscene = cutscene;
        this.player = player;
        this.isPlaying = false;
        this.currentTick = 0;
        this.currentSkipCount = 5 * 20;
        this.keyframesEntity = new ArrayList<>();
    }

    public void startSession() {

        keyframeGroupCounter.set(cutscene.getKeyframeGroupList().size());

        for(Subscene subscene : cutscene.getSubsceneList()) {
            subscene.start(player);
            for(Playback playback : subscene.getPlaybackList()) {
                LivingEntity entity = playback.getEntity();
                for(ServerPlayer serverPlayer : player.serverLevel().getServer().getPlayerList().getPlayers()) {
                    if(!serverPlayer.getName().getString().equals(player.getName().getString())) {
                        player.connection.send(new ClientboundPlayerInfoRemovePacket(List.of(entity.getUUID())));
                    }
                }
            }
        }

        for(KeyframeGroup keyframeGroup : cutscene.getKeyframeGroupList()) {
            for(Keyframe keyframe : keyframeGroup.getKeyframeList()) {
                keyframe.showKeyframeToClient(player);
                keyframesEntity.add(keyframe.getArmorStand());
            }
        }

        player.getInventory().clearContent();
        player.getInventory().setItem(0, CutsceneEditItems.createKeyframeGroup);
        player.getInventory().setItem(1, CutsceneEditItems.addKeyframe);
        player.getInventory().setItem(3, CutsceneEditItems.previousSecond);
        player.getInventory().setItem(4, CutsceneEditItems.cutscenePause);
        player.getInventory().setItem(5, CutsceneEditItems.nextSecond);
        player.getInventory().setItem(8, CutsceneEditItems.settings);
        pause();

    }

    public void stopSession() {

        for(Subscene subscene : cutscene.getSubsceneList()) {
            subscene.stop();
        }

        for(KeyframeGroup keyframeGroup : cutscene.getKeyframeGroupList()) {
            for(Keyframe keyframe : keyframeGroup.getKeyframeList()) {
                keyframe.removeKeyframeFromClient();
            }
        }

        player.getInventory().clearContent();

        isPlaying = false;

    }

    public void pause() {
        isPlaying = false;
        changePlayingPlaybackState();
        changeItem(CutsceneEditItems.cutscenePlaying, CutsceneEditItems.cutscenePause);
    }

    public void resume() {
        isPlaying = true;
        changeItem(CutsceneEditItems.cutscenePause, CutsceneEditItems.cutscenePlaying);
        changePlayingPlaybackState();
    }

    public void createKeyframeGroup() {
        KeyframeGroup keyframeGroup = new KeyframeGroup(keyframeGroupCounter.incrementAndGet());
        cutscene.getKeyframeGroupList().add(keyframeGroup);
        selectedKeyframeGroup = keyframeGroup;
        addKeyframe();
    }

    public boolean addKeyframe() {
        if(selectedKeyframeGroup == null) return false;
        int newId = keyframeCounter.incrementAndGet();
        Vec3 playerPos = player.position();
        PlayerCoord playerCoord = new PlayerCoord(playerPos.x(), playerPos.y() + player.getEyeHeight(), playerPos.z(), player.getXRot(), player.getYRot());
        Keyframe keyframe = new Keyframe(newId, playerCoord, 0, 0);
        if(!selectedKeyframeGroup.getKeyframeList().isEmpty()) {
            keyframe.setPathTime((currentTick / 20) * 1000L);
        } else {
            keyframe.showStartGroupText(player, selectedKeyframeGroup.getId());
        }
        selectedKeyframeGroup.getKeyframeList().add(keyframe);
        keyframe.showKeyframeToClient(player);
        return true;
    }

    public Keyframe getKeyframeByEntity(Entity entity) {
        for(KeyframeGroup keyframeGroup : cutscene.getKeyframeGroupList()) {
            for(Keyframe keyframe : keyframeGroup.getKeyframeList()) {
                if(keyframe.getArmorStand().getId() == entity.getId()) {
                    return keyframe;
                }
            }
        }
        return null;
     }

    public void openSettings() {
        CutsceneSettingsScreen screen = new CutsceneSettingsScreen(this, player);
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> minecraft.setScreen(screen));
    }

    private void changeItem(ItemStack previousItem, ItemStack newItem) {
        Inventory inventory = player.getInventory();
        for(int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);
            if(!itemStack.isEmpty()) {
                if(itemStack.getCustomName().getString().equals(previousItem.getCustomName().getString())) {
                    inventory.setItem(i, newItem);
                }
            }
        }
    }

    public void changeTimePosition(int newTick) {
        currentTick = newTick;
        for(Subscene subscene : cutscene.getSubsceneList()) {
            for(Playback playback : subscene.getPlaybackList()) {
                playback.changeLocationByTick(newTick);
            }
        }
    }

    public void nextSecondSkip() {
        changeTimePosition(currentTick + currentSkipCount);
    }

    public void previousSecondSkip() {
        changeTimePosition(Math.max(0, currentTick - currentSkipCount));
    }

    public void next() {
        if(isPlaying) currentTick++;
    }

    private void changePlayingPlaybackState() {
        for(Subscene subscene : cutscene.getSubsceneList()) {
            for(Playback playback : subscene.getPlaybackList()) {
                playback.setPlaying(isPlaying);
            }
        }
    }

    public Cutscene getCutscene() {
        return cutscene;
    }

    public void setCurrentSkipCount(int currentSkipCount) {
        this.currentSkipCount = currentSkipCount * 20;
        ItemStack oldPreviousSecond = CutsceneEditItems.previousSecond;
        ItemStack oldNextSecond = CutsceneEditItems.nextSecond;
        CutsceneEditItems.initSkipItems(player.registryAccess(), currentSkipCount);
        changeItem(oldPreviousSecond, CutsceneEditItems.previousSecond);
        changeItem(oldNextSecond, CutsceneEditItems.nextSecond);
    }

    public KeyframeGroup getSelectedKeyframeGroup() {
        return selectedKeyframeGroup;
    }

    public AtomicInteger getKeyframeGroupCounter() {
        return keyframeGroupCounter;
    }

    public List<Entity> getKeyframesEntity() {
        return keyframesEntity;
    }
}
