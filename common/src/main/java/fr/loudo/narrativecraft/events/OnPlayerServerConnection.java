package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.NarrativeUserOptions;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.items.CutsceneEditItems;
import fr.loudo.narrativecraft.keys.ModKeys;
import fr.loudo.narrativecraft.narrative.chapter.scenes.KeyframeControllerBase;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.screens.mainScreen.MainScreen;
import fr.loudo.narrativecraft.utils.ConstantsLink;
import fr.loudo.narrativecraft.utils.FakePlayer;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.net.URI;

public class OnPlayerServerConnection {

    public static void playerJoin(ServerPlayer player) {
        if(player instanceof FakePlayer) return;
        CutsceneEditItems.init(player.registryAccess());
        NarrativeUserOptions narrativeUserOptions = NarrativeCraftFile.loadUserOptions();
        if(narrativeUserOptions != null) {
            NarrativeCraftMod.getInstance().setNarrativeUserOptions(narrativeUserOptions);
        } else {
            NarrativeCraftMod.getInstance().setNarrativeUserOptions(new NarrativeUserOptions());
            NarrativeCraftFile.updateUserOptions();
        }
        if(NarrativeCraftMod.firstTime) {
            MutableComponent inkyLink = Component.literal("Inky").withStyle(style ->
                    style.withColor(ChatFormatting.YELLOW)
                            .withUnderlined(true)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, ConstantsLink.INKY))
            );
            MutableComponent docLink = Component.literal(ConstantsLink.DOCS).withStyle(style ->
                    style.withUnderlined(true)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, ConstantsLink.DOCS))
            );
            MutableComponent discordLink = Component.literal("discord").withStyle(style ->
                    style.withColor(ChatFormatting.BLUE)
                            .withUnderlined(true)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, ConstantsLink.DISCORD))
            );
            player.sendSystemMessage(Translation.message("user.first_time",
                    ModKeys.OPEN_STORY_MANAGER.getDefaultKey().getDisplayName(),
                    inkyLink,
                    docLink,
                    discordLink
            ));
        } else {
            if(!NarrativeCraftFile.getStoryFile().exists()) return;
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
