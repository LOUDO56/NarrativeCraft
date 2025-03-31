package fr.loudo.narrativecraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.loudo.narrativecraft.NarrativeCraft;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.scenes.Scene;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.session.PlayerSessionManager;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class SessionCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .then(Commands.literal("session")
                        .then(Commands.literal("set")
                                .then(Commands.argument("chapter_index", IntegerArgumentType.integer())
                                        .suggests(NarrativeCraft.getInstance().getChapterManager().getChapterSuggestions())
                                        .then(Commands.argument("scene_name", StringArgumentType.string())
                                                .suggests(NarrativeCraft.getInstance().getChapterManager().getSceneSuggestionsByChapter())
                                                .executes(context -> setSession(context, IntegerArgumentType.getInteger(context, "chapter_index"), StringArgumentType.getString(context, "scene_name")))
                                        )
                                )
                        )
                )
        );
    }

    private static int setSession(CommandContext<CommandSourceStack> context, int chapterIndex, String sceneName) {

        ServerPlayer player = context.getSource().getPlayer();

        if(!NarrativeCraft.getInstance().getChapterManager().chapterExists(chapterIndex)) {
            context.getSource().sendFailure(Translation.message("chapter.no_exists", chapterIndex));
            return 0;
        }

        Chapter chapter = NarrativeCraft.getInstance().getChapterManager().getChapterByIndex(chapterIndex);

        if(!chapter.sceneExists(sceneName)) {
            context.getSource().sendFailure(Translation.message("scene.no_exists", sceneName, chapterIndex));
            return 0;
        }

        Scene scene = chapter.getSceneByName(sceneName);

        PlayerSessionManager playerSessionManager = NarrativeCraft.getInstance().getPlayerSessionManager();
        PlayerSession playerSession = playerSessionManager.getPlayerSession(player);
        if(playerSession == null) {
            playerSession = new PlayerSession(player, chapter, scene);
            playerSessionManager.getPlayerSessions().add(playerSession);
        } else {
            playerSession.setChapter(chapter);
            playerSession.setScene(scene);
        }


        context.getSource().sendSuccess(() -> Translation.message("session.set", chapter.getIndex(), scene.getName()), false);

        return Command.SINGLE_SUCCESS;

    }
}
