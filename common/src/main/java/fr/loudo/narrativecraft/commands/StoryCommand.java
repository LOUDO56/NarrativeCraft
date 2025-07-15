package fr.loudo.narrativecraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.inkAction.InkAction;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.List;

public class StoryCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .then(Commands.literal("story")
                        .then(Commands.literal("validate")
                                .executes(StoryCommand::executeValidateStory)
                        )
                        .then(Commands.literal("play")
                                .executes(StoryCommand::playStory)
                                .then(Commands.literal("debug")
                                        .executes(StoryCommand::playStoryDebug)
                                )
                                .then(Commands.argument("chapter_index", IntegerArgumentType.integer())
                                        .suggests(NarrativeCraftMod.getInstance().getChapterManager().getChapterSuggestions())
                                        .then(Commands.argument("scene_name", StringArgumentType.string())
                                                .suggests(NarrativeCraftMod.getInstance().getChapterManager().getSceneSuggestionsByChapter())
                                                .executes(context -> playStoryChapterStory(context, IntegerArgumentType.getInteger(context, "chapter_index"), StringArgumentType.getString(context, "scene_name"), false))
                                                .then(Commands.literal("debug")
                                                        .executes(context -> playStoryChapterStory(context, IntegerArgumentType.getInteger(context, "chapter_index"), StringArgumentType.getString(context, "scene_name"), true))
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("stop")
                                .executes(StoryCommand::stopStory)
                        )
                )
        );
    }

    private static int validateStory(CommandContext<CommandSourceStack> context) {

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer localPlayer = minecraft.player;

        List<InkAction.ErrorLine> errorLineList = StoryHandler.validateStory();
        for(InkAction.ErrorLine errorLine : errorLineList) {
            localPlayer.displayClientMessage(errorLine.toMessage(), false);
        }

        if(!errorLineList.isEmpty()) {
            localPlayer.displayClientMessage((Translation.message("validation.found_errors", Component.literal(String.valueOf(errorLineList.size())).withColor(ChatFormatting.GOLD.getColor())).withColor(ChatFormatting.RED.getColor())), false);
        }

        return errorLineList.isEmpty() ? Command.SINGLE_SUCCESS : 0;
    }

    private static int executeValidateStory(CommandContext<CommandSourceStack> context) {

        if(validateStory(context) == 0) return 0;
        context.getSource().sendSystemMessage(Translation.message("validation.no_errors").withColor(ChatFormatting.GREEN.getColor()));

        return Command.SINGLE_SUCCESS;
    }


    private static int playStory(CommandContext<CommandSourceStack> context) {

        if(validateStory(context) == 0) return 0;

        StoryHandler storyHandler = new StoryHandler();
        storyHandler.start();

        return Command.SINGLE_SUCCESS;
    }

    private static int playStoryChapterStory(CommandContext<CommandSourceStack> context, int chapterIndex, String sceneName, boolean debug) {

        if(validateStory(context) == 0) return 0;

        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getChapterByIndex(chapterIndex);
        Scene scene = chapter.getSceneByName(sceneName);
        StoryHandler storyHandler = new StoryHandler(chapter, scene);
        storyHandler.setDebugMode(debug);
        storyHandler.start();

        return Command.SINGLE_SUCCESS;
    }

    private static int playStoryDebug(CommandContext<CommandSourceStack> context) {

        if(validateStory(context) == 0) return 0;

        StoryHandler storyHandler = new StoryHandler();
        storyHandler.setDebugMode(true);
        storyHandler.start();
        return Command.SINGLE_SUCCESS;
    }

    private static int stopStory(CommandContext<CommandSourceStack> context) {

        StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
        if(storyHandler == null || !storyHandler.isRunning()) {
            context.getSource().sendFailure(Translation.message("story.load.no_story_playing"));
            return 0;
        }

        storyHandler.stop(true);
        context.getSource().sendSuccess(() -> Component.literal("Story stopped."), false);

        return Command.SINGLE_SUCCESS;
    }

}
