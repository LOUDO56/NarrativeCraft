package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraft;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.recordings.playback.PlaybackHandler;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.RecordingHandler;
import fr.loudo.narrativecraft.narrative.recordings.MovementData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NarrativeCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerTickEvent {
    private static final RecordingHandler RECORDING_HANDLER = NarrativeCraft.getInstance().getRecordingHandler();
    private static final PlaybackHandler PLAYBACK_HANDLER = NarrativeCraft.getInstance().getPlaybackHandler();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        for(Recording recording : RECORDING_HANDLER.getRecordings()) {
            if(recording.isRecording()) {
                ServerPlayer player = recording.getPlayer();
                MovementData currentLoc = new MovementData(
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        player.getXRot(),
                        player.getYRot(),
                        player.getYHeadRot(),
                        player.onGround()
                );
                recording.getLocations().add(currentLoc);
            }
        }
        for(Playback playback : PLAYBACK_HANDLER.getPlaybacks()) {
            if(playback.isPlaying()) {
                playback.next();
            }
        }
    }

}
