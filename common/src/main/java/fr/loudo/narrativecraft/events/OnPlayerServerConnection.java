package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.items.CutsceneEditItems;
import fr.loudo.narrativecraft.narrative.chapter.scenes.KeyframeControllerBase;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.screens.mainScreen.MainScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;

public class OnPlayerServerConnection {

    public static void playerJoin(ServerPlayer player) {
        CutsceneEditItems.init(player.registryAccess());
        MainScreen mainScreen = new MainScreen();
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(mainScreen));
    }

    public static void playerLeave(ServerPlayer player) {
        PlayerSession playerSession = NarrativeCraftMod.getInstance().getPlayerSession();
        KeyframeControllerBase keyframeControllerBase = playerSession.getKeyframeControllerBase();
        if(keyframeControllerBase != null) {
            keyframeControllerBase.stopSession(true);
        }
        Recording recording = NarrativeCraftMod.getInstance().getRecordingHandler().getRecordingOfPlayer(player);
        if(recording != null) {
            recording.stop();
        }
        StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
        if(storyHandler != null) {
            NarrativeCraftMod.server.execute(storyHandler::stop);
        }


    }

}
