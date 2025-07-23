package fr.loudo.narrativecraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.inkAction.validation.ErrorLine;
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
        dispatcher.register(Commands.literal("nc").requires(commandSourceStack -> commandSourceStack.getPlayer().hasPermissions(2))
                .then(Commands.literal("story")
                        .then(Commands.literal("validate")
                                .executes(StoryCommand::executeValidateStory)
                        )
                        .then(Commands.literal("play")
                                .then(Commands.argument("chapter_index", IntegerArgumentType.integer())
                                        .suggests(NarrativeCraftMod.getInstance().getChapterManager().getChapterSuggestions())
                                        .then(Commands.argument("scene_name", StringArgumentType.string())
                                                .suggests(NarrativeCraftMod.getInstance().getChapterManager().getSceneSuggestionsByChapter())
                                                .executes(context -> playStoryChapterStory(context, IntegerArgumentType.getInteger(context, "chapter_index"), StringArgumentType.getString(context, "scene_name"), false))
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

        List<ErrorLine> errorLineList = StoryHandler.validateStory();
        for(ErrorLine errorLine : errorLineList) {
            localPlayer.displayClientMessage(errorLine.toMessage(), false);
        }

        int errorSize = errorLineList.stream().filter(errorLine -> !errorLine.isWarn()).toList().size();
        int warnSize = errorLineList.stream().filter(ErrorLine::isWarn).toList().size();
        if(errorSize > 0) {
            localPlayer.displayClientMessage((Translation.message("validation.found_errors", Component.literal(String.valueOf(errorSize)).withColor(ChatFormatting.GOLD.getColor())).withColor(ChatFormatting.RED.getColor())), false);
        }
        if(warnSize > 0) {
            localPlayer.displayClientMessage((Translation.message("validation.found_warns", Component.literal(String.valueOf(warnSize)).withColor(ChatFormatting.GOLD.getColor())).withColor(ChatFormatting.YELLOW.getColor())), false);
        }

        return errorSize > 0 ? 0 : Command.SINGLE_SUCCESS;
    }

    private static int executeValidateStory(CommandContext<CommandSourceStack> context) {

        if(validateStory(context) == 0) return 0;
        context.getSource().sendSystemMessage(Translation.message("validation.no_errors").withColor(ChatFormatting.GREEN.getColor()));

        return Command.SINGLE_SUCCESS;
    }

    private static int playStoryChapterStory(CommandContext<CommandSourceStack> context, int chapterIndex, String sceneName, boolean debug) {

        if(!NarrativeCraftFile.getStoryFile().exists()) {
            context.getSource().sendFailure(Translation.message("story.no_exists"));
            return 0;
        }
        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getChapterByIndex(chapterIndex);
        if(chapter == null) {
            context.getSource().sendFailure(Translation.message("chapter.no_exists", chapterIndex));
            return 0;
        }
        Scene scene = chapter.getSceneByName(sceneName);
        if(scene == null) {
            context.getSource().sendFailure(Translation.message("scene.no_exists", sceneName, chapterIndex));
            return 0;
        }
        if(validateStory(context) == 0) return 0;
        StoryHandler storyHandler = new StoryHandler(chapter, scene);
        storyHandler.setDebugMode(true);
        storyHandler.start();

        return Command.SINGLE_SUCCESS;
    }

    private static int stopStory(CommandContext<CommandSourceStack> context) {
        if(!context.getSource().getPlayer().hasPermissions(2)) return 0;

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
