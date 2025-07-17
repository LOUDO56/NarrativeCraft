package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.items.CutsceneEditItems;
import fr.loudo.narrativecraft.keys.ModKeys;
import fr.loudo.narrativecraft.narrative.chapter.scenes.KeyframeControllerBase;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.screens.mainScreen.MainScreen;
import fr.loudo.narrativecraft.utils.FakePlayer;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.*;
import net.minecraft.server.commands.TellRawCommand;
import net.minecraft.server.level.ServerPlayer;

import java.net.URI;

public class OnPlayerServerConnection {

    public static void playerJoin(ServerPlayer player) {
        if(player instanceof FakePlayer) return;
        CutsceneEditItems.init(player.registryAccess());
        if(NarrativeCraftMod.firstTime) {
            MutableComponent inkyLink = Component.literal("Inky").withStyle(style ->
                    style.withColor(ChatFormatting.YELLOW)
                            .withUnderlined(true)
                            .withClickEvent(new ClickEvent.OpenUrl(URI.create("https://github.com/inkle/inky/releases/")))
            );
            MutableComponent docLink = Component.literal("https://doc.com").withStyle(style ->
                    style.withUnderlined(true).
                            withClickEvent(new ClickEvent.OpenUrl(URI.create("https://doc.com")))
            );
            MutableComponent discordLink = Component.literal("discord").withStyle(style ->
                    style.withColor(ChatFormatting.BLUE)
                            .withUnderlined(true)
                            .withClickEvent(new ClickEvent.OpenUrl(URI.create("https://discord.gg/E3zzNv79DN")))
            );
            player.sendSystemMessage(Translation.message("user.first_time",
                    ModKeys.OPEN_STORY_MANAGER.getDefaultKey().getDisplayName(),
                    inkyLink,
                    docLink,
                    discordLink
            ));
        } else {
            NarrativeCraftFile.loadUserOptions();
            MainScreen mainScreen = new MainScreen(false, false);
            Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(mainScreen));
        }
    }

    public static void playerLeave(ServerPlayer player) {
        if(player instanceof FakePlayer) return;
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
            storyHandler.stop(true);
        }


    }

}
