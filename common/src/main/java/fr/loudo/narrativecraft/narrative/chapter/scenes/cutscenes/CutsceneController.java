package fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes;

import fr.loudo.narrativecraft.items.CutsceneEditItems;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;
import fr.loudo.narrativecraft.screens.CutsceneSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CutsceneController {

    private Cutscene cutscene;
    private ServerPlayer player;
    private boolean isPlaying;
    private int currentTick;
    private int currentSkipCount;

    public CutsceneController(Cutscene cutscene, ServerPlayer player) {
        this.cutscene = cutscene;
        this.player = player;
        this.isPlaying = false;
        this.currentTick = 0;
        this.currentSkipCount = 5 * 20;
    }

    public void startSession() {

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

        player.getInventory().clearContent();
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
}
