package fr.loudo.narrativecraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class StoryCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .then(Commands.literal("story")
                        .then(Commands.literal("play")
                                .executes(StoryCommand::playStory)
                        )
                )
        );
    }

    private static int playStory(CommandContext<CommandSourceStack> context) {

        Chapter firstChapter = NarrativeCraftMod.getInstance().getChapterManager().getChapters().getFirst();
        Scene firstScene = firstChapter.getSceneList().getFirst();
        NarrativeCraftMod.getInstance().setStoryHandler(new StoryHandler(firstChapter, firstScene));

        return Command.SINGLE_SUCCESS;
    }

}
