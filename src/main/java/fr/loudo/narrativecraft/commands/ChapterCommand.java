package fr.loudo.narrativecraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.loudo.narrativecraft.NarrativeCraft;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ChapterCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .then(Commands.literal("chapter")
                        .then(Commands.literal("create")
                                .then(Commands.argument("chapter_index", IntegerArgumentType.integer())
                                        .then(Commands.argument("chapter_name", StringArgumentType.string())
                                                .executes(context -> {

                                                    int index = IntegerArgumentType.getInteger(context, "chapter_index");
                                                    String name = StringArgumentType.getString(context, "chapter_name");

                                                    return createChapter(context, index, name);
                                                }))
                                        .executes(context -> {

                                            int index = IntegerArgumentType.getInteger(context, "chapter_index");

                                            return createChapter(context, index, "");
                                        }))
                                )
                        )
        );
    }

    private static int createChapter(CommandContext<CommandSourceStack> context, int index, String name) {

        if(NarrativeCraft.getChapterManager().chapterExists(index)) {
            Chapter chapter = NarrativeCraft.getChapterManager().getChapterByIndex(index);
            context.getSource().sendFailure(Translation.message("chapter.already_exists", chapter.getIndex()));
            return 0;
        }

        Chapter chapter;
        if(name.isEmpty()) {
            chapter = new Chapter(index);
        } else {
            chapter = new Chapter(index, name);
        }

        if(NarrativeCraft.getChapterManager().addChapter(chapter)) {
            context.getSource().sendSuccess(() -> Translation.message("chapter.create.success", chapter.getIndex()), true);
        } else {
            context.getSource().sendSuccess(() -> Translation.message("chapter.create.fail", chapter.getIndex()), true);
        }

        return Command.SINGLE_SUCCESS;
    }
}
