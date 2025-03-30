package fr.loudo.narrativecraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.loudo.narrativecraft.NarrativeCraft;
import fr.loudo.narrativecraft.narrative.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.io.IOException;

public class SceneCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .then(Commands.literal("scene")
                        .then(Commands.literal("create")
                                .then(Commands.argument("chapter_index", IntegerArgumentType.integer())
                                        .suggests(NarrativeCraft.getChapterManager().getChapterSuggestions())
                                        .then(Commands.argument("scene_name", StringArgumentType.string())
                                                .executes(context -> createScene(context, IntegerArgumentType.getInteger(context, "chapter_index"), StringArgumentType.getString(context, "scene_name")))
                                        )
                                )
                        )
                )
        );
    }


    private static int createScene(CommandContext<CommandSourceStack> context, int chapterIndex, String sceneName) {

        if(!NarrativeCraft.getChapterManager().chapterExists(chapterIndex)) {
            context.getSource().sendFailure(Translation.message("chapter.no_exists", chapterIndex));
            return 0;
        }

        if(NarrativeCraft.getSceneManager().sceneExists(sceneName)) {
            Scene scene = NarrativeCraft.getSceneManager().getSceneByName(sceneName);
            context.getSource().sendFailure(Translation.message("scene.already_exists", scene.getName(), scene.getChapter().getIndex()));
            return 0;
        }

        Chapter chapter = NarrativeCraft.getChapterManager().getChapterByIndex(chapterIndex);

        Scene scene = new Scene(chapter, sceneName);
        try {
            chapter.addScene(scene);
            context.getSource().sendSuccess(() -> Translation.message("scene.create.success", scene.getName(), chapter.getIndex()), true);
        } catch (IOException e) {
            context.getSource().sendSuccess(() -> Translation.message("scene.create.fail", scene.getName(), chapter.getIndex(), e), true);
        }

        return Command.SINGLE_SUCCESS;
    }
}
