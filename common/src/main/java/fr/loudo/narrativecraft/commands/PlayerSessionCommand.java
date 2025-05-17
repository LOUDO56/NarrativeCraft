package fr.loudo.narrativecraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;


public class PlayerSessionCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .then(Commands.literal("session")
                        .then(Commands.literal("clear")
                                .executes(PlayerSessionCommand::clearSession)
                        )
                        .then(Commands.literal("set")
                                .then(Commands.argument("chapter_index", IntegerArgumentType.integer())
                                        .suggests(NarrativeCraftMod.getInstance().getChapterManager().getChapterSuggestions())
                                        .then(Commands.argument("scene_name", StringArgumentType.string())
                                                .suggests(NarrativeCraftMod.getInstance().getChapterManager().getSceneSuggestionsByChapter())
                                                .executes(context -> setSession(context, IntegerArgumentType.getInteger(context, "chapter_index"), StringArgumentType.getString(context, "scene_name")))
                                        )
                                )
                        )
                )
        );
    }

    private static int setSession(CommandContext<CommandSourceStack> context, int chapterIndex, String sceneName) {

        ServerPlayer player = context.getSource().getPlayer();

        if(!NarrativeCraftMod.getInstance().getChapterManager().chapterExists(chapterIndex)) {
            context.getSource().sendFailure(Translation.message("chapter.no_exists", chapterIndex));
            return 0;
        }

        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getChapterByIndex(chapterIndex);

        if(!chapter.sceneExists(sceneName)) {
            context.getSource().sendFailure(Translation.message("scene.no_exists", sceneName, chapterIndex));
            return 0;
        }

        Scene scene = chapter.getSceneByName(sceneName);
        NarrativeCraftMod.getInstance().getPlayerSessionManager().setSession(player, chapter, scene);
        context.getSource().sendSuccess(() -> Translation.message("session.set", chapter.getIndex(), scene.getName()), false);

        return Command.SINGLE_SUCCESS;

    }

    private static int clearSession(CommandContext<CommandSourceStack> context) {

        if(NarrativeCraftMod.getInstance().getPlayerSessionManager().clearSession(context.getSource().getPlayer())) {
            context.getSource().sendSuccess(() -> Translation.message("session.cleared"), false);
        } else {
            context.getSource().sendFailure(Translation.message("session.cleared.fail"));
        }

        return Command.SINGLE_SUCCESS;
    }

}
