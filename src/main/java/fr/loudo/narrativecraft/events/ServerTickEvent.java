package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraft;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.RecordingHandler;
import fr.loudo.narrativecraft.utils.Location;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NarrativeCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerTickEvent {
    private static final RecordingHandler RECORDING_HANDLER = NarrativeCraft.getInstance().getRecordingHandler();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        for(Recording recording : RECORDING_HANDLER.getRecordings()) {
            if(recording.isRecording()) {
                ServerPlayer player = recording.getPlayer();
                Location currentLoc = new Location(
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        player.getXRot(),
                        player.getYRot(),
                        player.getYHeadRot()
                );
                System.out.println("Recording...");
            }
        }
    }

}
