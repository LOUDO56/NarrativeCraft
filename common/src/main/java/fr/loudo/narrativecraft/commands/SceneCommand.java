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

public class SceneCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .then(Commands.literal("scene")
                        .then(Commands.literal("create")
                                .then(Commands.argument("chapter_index", IntegerArgumentType.integer())
                                        .suggests(NarrativeCraftMod.getInstance().getChapterManager().getChapterSuggestions())
                                        .then(Commands.argument("scene_name", StringArgumentType.string())
                                                .executes(context -> createScene(context, IntegerArgumentType.getInteger(context, "chapter_index"), StringArgumentType.getString(context, "scene_name")))
                                        )
                                )
                        )
                        .then(Commands.literal("remove")
                                .then(Commands.argument("chapter_index", IntegerArgumentType.integer())
                                        .suggests(NarrativeCraftMod.getInstance().getChapterManager().getChapterSuggestions())
                                        .then(Commands.argument("scene_name", StringArgumentType.string())
                                                .suggests(NarrativeCraftMod.getInstance().getChapterManager().getSceneSuggestionsByChapter())
                                                .executes(context -> removeScene(context, IntegerArgumentType.getInteger(context, "chapter_index"), StringArgumentType.getString(context, "scene_name")))
                                        )
                                )
                        )
                )
        );
    }

    private static int createScene(CommandContext<CommandSourceStack> context, int chapterIndex, String sceneName) {

        if(!NarrativeCraftMod.getInstance().getChapterManager().chapterExists(chapterIndex)) {
            context.getSource().sendFailure(Translation.message("chapter.no_exists", chapterIndex));
            return 0;
        }

        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getChapterByIndex(chapterIndex);

        if(chapter.sceneExists(sceneName)) {
            Scene scene = chapter.getSceneByName(sceneName);
            context.getSource().sendFailure(Translation.message("scene.already_exists", scene.getName(), scene.getChapter().getIndex()));
            return 0;
        }

        Scene scene = new Scene(chapter, sceneName);
        if(chapter.addScene(scene)) {
            context.getSource().sendSuccess(() -> Translation.message("scene.create.success", scene.getName(), chapter.getIndex()), true);
        } else {
            context.getSource().sendFailure(Translation.message("scene.create.fail", scene.getName(), chapter.getIndex()));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int removeScene(CommandContext<CommandSourceStack> context, int chapterIndex, String sceneName) {

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
        if(chapter.removeScene(scene)) {
            context.getSource().sendSuccess(() -> Translation.message("scene.delete.success", scene.getName(), chapter.getIndex()), true);
        } else {
            context.getSource().sendFailure(Translation.message("scene.delete.fail", scene.getName(), chapter.getIndex()));
        }

        return Command.SINGLE_SUCCESS;
    }
}
