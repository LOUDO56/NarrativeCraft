package fr.loudo.narrativecraft.registers;

import fr.loudo.narrativecraft.commands.*;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class CommandsRegister {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((commandDispatcher, commandBuildContext, commandSelection) -> {
            ChapterCommand.register(commandDispatcher);
            SceneCommand.register(commandDispatcher);
            AnimationCommand.register(commandDispatcher);
            CharacterCommand.register(commandDispatcher);
            RecordCommand.register(commandDispatcher);
            SessionCommand.register(commandDispatcher);
            StoryCommand.register(commandDispatcher);
            SubsceneCommand.register(commandDispatcher);
            CutsceneCommand.register(commandDispatcher);
        });
    }

}
