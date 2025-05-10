package fr.loudo.narrativecraft.registers;

import fr.loudo.narrativecraft.commands.*;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class CommandsRegister {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((commandDispatcher, commandBuildContext, commandSelection) -> {
            TestCommand.register(commandDispatcher);
        });
    }

}
