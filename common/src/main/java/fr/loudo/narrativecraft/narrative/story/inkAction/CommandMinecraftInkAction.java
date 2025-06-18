package fr.loudo.narrativecraft.narrative.story.inkAction;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;
import java.util.Optional;

public class CommandMinecraftInkAction extends InkAction {

    private String commandMinecraft;

    public CommandMinecraftInkAction() {}

    public CommandMinecraftInkAction(StoryHandler storyHandler) {
        super(storyHandler);
    }

    @Override
    public InkActionResult execute(String[] command) {
        if(command.length == 1) return InkActionResult.ERROR;
        commandMinecraft = String.join(" ", Arrays.stream(command).toList().subList(1, command.length));
        CommandSourceStack commandSourceStack = getCommandSourceStack();
        try {
            NarrativeCraftMod.server.getCommands().getDispatcher().execute(commandMinecraft, commandSourceStack);
        } catch (CommandSyntaxException e) {
            return InkActionResult.ERROR;
        }
        sendDebugDetails();
        return InkActionResult.PASS;
    }

    private CommandSourceStack getCommandSourceStack() {
        ServerPlayer serverPlayer = storyHandler.getPlayerSession().getPlayer();
        CommandSource commandSource = CommandSource.NULL;
        return new CommandSourceStack(
                commandSource,
                serverPlayer.position(),
                serverPlayer.getRotationVector(),
                serverPlayer.serverLevel(),
                4,
                serverPlayer.getName().getString(),
                serverPlayer.getDisplayName(),
                serverPlayer.server,
                serverPlayer
        );
    }

    @Override
    void sendDebugDetails() {
        if(storyHandler.isDebugMode()) {
            Minecraft.getInstance().player.displayClientMessage(
                    Translation.message("debug.minecraft_command_executed", commandMinecraft),
                    false
            );
        }
    }

    @Override
    public ErrorLine validate(String[] command, int line, String lineText, Scene scene) {
        if(command.length == 1) {
            return new ErrorLine(
                    line,
                    scene,
                    "validation.missing_minecraft_command",
                    lineText
            );
        }
        String commandMinecraft = String.join(" ", Arrays.stream(command).toList().subList(1, command.length));
        commandMinecraft = commandMinecraft.replace("\\{", "{");
        commandMinecraft = commandMinecraft.replace("\\}", "}");
        CommandSourceStack commandSourceStack = Utils.getServerPlayerByUUID(Minecraft.getInstance().player.getUUID()).createCommandSourceStack();
        ParseResults<CommandSourceStack> parse = NarrativeCraftMod.server.getCommands().getDispatcher().parse(new StringReader(commandMinecraft), commandSourceStack);
        if (parse.getReader().canRead()) {
            if (parse.getExceptions().size() == 1) {
                return new ErrorLine(
                        line,
                        scene,
                        Translation.message("validation.invalid_command", parse.getExceptions().values().iterator().next().getMessage()).getString(),
                        lineText
                );
            }
        }
        return null;
    }
}
