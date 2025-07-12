package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.KeyframeControllerBase;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.recordings.playback.PlaybackHandler;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;

public class OnServerTick {

    private static final PlaybackHandler PLAYBACK_HANDLER = NarrativeCraftMod.getInstance().getPlaybackHandler();

    public static void serverTick() {
        if(Minecraft.getInstance().isPaused()) return;
        for (Playback playback : PLAYBACK_HANDLER.getPlaybacks()) {
            if (playback.isPlaying()) {
                playback.next();
            }
        }

        PlayerSession playerSession = NarrativeCraftMod.getInstance().getPlayerSession();
        KeyframeControllerBase keyframeControllerBase = playerSession.getKeyframeControllerBase();
        if(keyframeControllerBase instanceof CutsceneController cutsceneController) {
            cutsceneController.next();
        }
    }
}
