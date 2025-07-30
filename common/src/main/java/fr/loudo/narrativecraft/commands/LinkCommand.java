package fr.loudo.narrativecraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import fr.loudo.narrativecraft.utils.ConstantsLink;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.net.URI;

public class LinkCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .requires(commandSourceStack -> commandSourceStack.getPlayer().hasPermissions(2))
                .then(Commands.literal("link")
                        .then(Commands.literal("discord").executes(commandContext -> openLink(ConstantsLink.DISCORD)))
                        .then(Commands.literal("docs").executes(commandContext -> openLink(ConstantsLink.DOCS)))
                        .then(Commands.literal("github").executes(commandContext -> openLink(ConstantsLink.GITHUB)))

                )
        );
    }

    private static int openLink(String url) {
        Util.getPlatform().openUri(URI.create(url));
        return Command.SINGLE_SUCCESS;
    }


}
