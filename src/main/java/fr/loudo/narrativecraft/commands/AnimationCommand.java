package fr.loudo.narrativecraft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import fr.loudo.narrativecraft.NarrativeCraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class AnimationCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .then(Commands.literal("animation")
                        .then(Commands.literal("create")
                                .then(Commands.argument("chapter_index", IntegerArgumentType.integer())
                                        .suggests(NarrativeCraft.getInstance().getChapterManager().getChapterSuggestions())
                                        .then(Commands.argument("scene_name", StringArgumentType.string())
                                                .suggests(NarrativeCraft.getInstance().getChapterManager().getSceneSuggestionsByChapter())
                                                .then(Commands.argument("animation_name", StringArgumentType.string())
                                                        .executes(context -> 1)
                                                )
                                        )
                                )
                        )
                )
        );
    }

}
