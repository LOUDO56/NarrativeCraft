package fr.loudo.narrativecraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.loudo.narrativecraft.NarrativeCraftManager;
import fr.loudo.narrativecraft.narrative.character.Character;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CharacterCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .then(Commands.literal("character")
                        .then(Commands.literal("create")
                                .then(Commands.argument("character_name", StringArgumentType.string())
                                        .executes(context -> createCharacter(context, StringArgumentType.getString(context, "character_name")))
                                )
                        )
                )
        );
    }

    private static int createCharacter(CommandContext<CommandSourceStack> context, String characterName) {

        if(NarrativeCraftManager.getInstance().getCharacterManager().characterExists(characterName)) {
            Character character = NarrativeCraftManager.getInstance().getCharacterManager().getCharacterByName(characterName);
            context.getSource().sendFailure(Translation.message("character.already_exists", character.getName()));
            return 0;
        }

        Character character = new Character(characterName);
        if(NarrativeCraftManager.getInstance().getCharacterManager().addCharacter(character)) {
            context.getSource().sendSuccess(() -> Translation.message("character.create.success", characterName, characterName.toLowerCase()), true);
        } else {
            context.getSource().sendFailure(Translation.message("character.create.fail"));
        }

        return Command.SINGLE_SUCCESS;
    }

}
