package fr.loudo.narrativecraft.narrative.story.inkAction;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;

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
        ServerPlayer serverPlayer = storyHandler.getPlayerSession().getPlayer();
        CommandSourceStack commandSourceStack = serverPlayer.createCommandSourceStack();
        try {
            NarrativeCraftMod.server.getCommands().getDispatcher().execute(commandMinecraft, commandSourceStack);
        } catch (CommandSyntaxException e) {
            return InkActionResult.ERROR;
        }
        return InkActionResult.PASS;
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
        return null;
    }
}
