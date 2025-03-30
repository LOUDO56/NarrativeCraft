package fr.loudo.narrativecraft.story.commands;

import com.bladecoder.ink.runtime.Story;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.loudo.narrativecraft.NarrativeCraft;
import fr.loudo.narrativecraft.story.Chapter;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

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
            context.getSource().sendFailure(Translation.message("chapter.create.exists", chapter.getIndex()));
            return 0;
        }

        Chapter chapter;
        if(name.isEmpty()) {
            chapter = new Chapter(index);
        } else {
            chapter = new Chapter(index, name);
        }
        NarrativeCraft.getChapterManager().addChapter(chapter);
        context.getSource().sendSuccess(() -> Translation.message("chapter.create.success", chapter.getIndex()), true);

        try {
            Story story = new Story("{\"inkVersion\":21,\"root\":[[\"^Once upon a time...\",\"\\n\",[\"ev\",{\"^->\":\"0.2.$r1\"},{\"temp=\":\"$r\"},\"str\",{\"->\":\".^.s\"},[{\"#n\":\"$r1\"}],\"/str\",\"/ev\",{\"*\":\"0.c-0\",\"flg\":18},{\"s\":[\"^There were two choices.\",{\"->\":\"$r\",\"var\":true},null]}],[\"ev\",{\"^->\":\"0.3.$r1\"},{\"temp=\":\"$r\"},\"str\",{\"->\":\".^.s\"},[{\"#n\":\"$r1\"}],\"/str\",\"/ev\",{\"*\":\"0.c-1\",\"flg\":18},{\"s\":[\"^There were four lines of content.\",{\"->\":\"$r\",\"var\":true},null]}],{\"c-0\":[\"ev\",{\"^->\":\"0.c-0.$r2\"},\"/ev\",{\"temp=\":\"$r\"},{\"->\":\"0.2.s\"},[{\"#n\":\"$r2\"}],\"\\n\",{\"->\":\"0.g-0\"},{\"#f\":5}],\"c-1\":[\"ev\",{\"^->\":\"0.c-1.$r2\"},\"/ev\",{\"temp=\":\"$r\"},{\"->\":\"0.3.s\"},[{\"#n\":\"$r2\"}],\"\\n\",{\"->\":\"0.g-0\"},{\"#f\":5}],\"g-0\":[\"^They lived happily ever after.\",\"\\n\",\"end\",[\"done\",{\"#f\":5,\"#n\":\"g-1\"}],{\"#f\":5}]}],\"done\",{\"#f\":1}],\"listDefs\":{}}");
            context.getSource().getPlayer().sendSystemMessage(Component.literal(story.Continue()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Command.SINGLE_SUCCESS;
    }
}
