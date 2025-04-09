package fr.loudo.narrativecraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import fr.loudo.narrativecraft.screens.CutsceneSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class TestCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("test")
                .then(Commands.literal("screen")
                        .then(Commands.literal("changeSecondValue")
                                .executes(TestCommand::openScreenChangeSecond)
                        )
                )
        );
    }

    private static int openScreenChangeSecond(CommandContext<CommandSourceStack> context) {
        CutsceneSettingsScreen cutsceneSettingsScreen = new CutsceneSettingsScreen();
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(cutsceneSettingsScreen));
        return Command.SINGLE_SUCCESS;
    }
}
