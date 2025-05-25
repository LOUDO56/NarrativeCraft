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
import net.minecraft.network.chat.Component;

public class StoryCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .then(Commands.literal("story")
                        .then(Commands.literal("play")
                                .executes(StoryCommand::playStory)
                                .then(Commands.literal("debug")
                                        .executes(StoryCommand::playStoryDebug)
                                )
                        )
                        .then(Commands.literal("stop")
                                .executes(StoryCommand::stopStory)
                        )
                )
        );
    }


    private static int playStory(CommandContext<CommandSourceStack> context) {

        Chapter firstChapter = NarrativeCraftMod.getInstance().getChapterManager().getChapters().getFirst();
        Scene firstScene = firstChapter.getSceneList().getFirst();
        StoryHandler storyHandler = new StoryHandler(firstChapter, firstScene);
        storyHandler.start();
        NarrativeCraftMod.getInstance().setStoryHandler(storyHandler);

        return Command.SINGLE_SUCCESS;
    }

    private static int playStoryDebug(CommandContext<CommandSourceStack> context) {
        Chapter firstChapter = NarrativeCraftMod.getInstance().getChapterManager().getChapters().getFirst();
        Scene firstScene = firstChapter.getSceneList().getFirst();
        StoryHandler storyHandler = new StoryHandler(firstChapter, firstScene, true);
        storyHandler.start();
        NarrativeCraftMod.getInstance().setStoryHandler(storyHandler);

        return Command.SINGLE_SUCCESS;
    }

    private static int stopStory(CommandContext<CommandSourceStack> context) {

        StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
        if(storyHandler == null) {
            context.getSource().sendFailure(Component.literal("No story is playing."));
            return 0;
        }

        storyHandler.stop();
        NarrativeCraftMod.getInstance().setStoryHandler(null);
        context.getSource().sendSuccess(() -> Component.literal("Story stopped."), false);

        return Command.SINGLE_SUCCESS;
    }

}
