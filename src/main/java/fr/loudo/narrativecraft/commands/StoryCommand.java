package fr.loudo.narrativecraft.commands;

import com.bladecoder.ink.runtime.Story;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.loudo.narrativecraft.NarrativeCraft;
import fr.loudo.narrativecraft.narrative.story.InkTagTranslators;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

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

        Story story = NarrativeCraft.story;
        List<String> tags = new ArrayList<>();
        String dialogue = "";

        while (story.canContinue()) {

            try {
                dialogue = story.Continue();
                context.getSource().getPlayer().sendSystemMessage(Component.literal(dialogue));
            } catch (Exception ignored) {

            }

            try {
                tags = story.getCurrentTags();
                if(!tags.isEmpty()) {
                    for(String command : tags) {
                        InkTagTranslators.execute(command);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

        try {
            NarrativeCraft.story = new Story("{\"inkVersion\":21,\"root\":[[\"#\",\"^animation play chapter-1.village.village_jake\",\"/#\",\"^Once upon a time...\",\"\\n\",[\"done\",{\"#f\":5,\"#n\":\"g-0\"}],null],\"done\",{\"#f\":1}],\"listDefs\":{}}");
        } catch (Exception ignored) {

        }

        return Command.SINGLE_SUCCESS;
    }
}
