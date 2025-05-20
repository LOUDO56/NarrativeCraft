package fr.loudo.narrativecraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import fr.loudo.narrativecraft.screens.storyManager.chapters.ChaptersScreen;
import fr.loudo.narrativecraft.screens.storyManager.characters.CharactersScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class OpenScreenCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .then(Commands.literal("open")
                        .then(Commands.literal("story_manager")
                                .executes(OpenScreenCommand::openStoryManager)
                        )
                        .then(Commands.literal("character_manager")
                                .executes(OpenScreenCommand::openCharacterManager)
                        )
                )
        );
    }

    private static int openStoryManager(CommandContext<CommandSourceStack> context) {
        ChaptersScreen screen = new ChaptersScreen();
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(screen));
        return Command.SINGLE_SUCCESS;
    }

    private static int openCharacterManager(CommandContext<CommandSourceStack> context) {
        CharactersScreen screen = new CharactersScreen();
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(screen));
        return Command.SINGLE_SUCCESS;
    }
}
