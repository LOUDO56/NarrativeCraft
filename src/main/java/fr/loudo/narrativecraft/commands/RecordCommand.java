package fr.loudo.narrativecraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.loudo.narrativecraft.NarrativeCraft;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class RecordCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .then(Commands.literal("record")
                        .then(Commands.literal("start")
                                .executes(RecordCommand::startRecording)
                        )
                        .then(Commands.literal("stop")
                                .executes(RecordCommand::stopRecording)
                        )
                )
        );
    }

    private static int startRecording(CommandContext<CommandSourceStack> context) {

        ServerPlayer player = context.getSource().getPlayer();
        PlayerSession playerSession = NarrativeCraft.getInstance().getPlayerSessionManager().getPlayerSession(player);
        if(playerSession.getChapter() == null || playerSession.getScene() == null) {
            context.getSource().sendFailure(Translation.message("record.start.no_session"));
            return 0;
        }

        if(NarrativeCraft.getInstance().getRecordingHandler().isPlayerRecording(player)) {
            context.getSource().sendFailure(Translation.message("record.start.fail"));
            return 0;
        }

        Recording recording = new Recording(context.getSource().getPlayer());
        recording.start();

        context.getSource().sendSuccess(() -> Translation.message("record.start.success"), true);

        return Command.SINGLE_SUCCESS;
    }


    private static int stopRecording(CommandContext<CommandSourceStack> context) {

        ServerPlayer player = context.getSource().getPlayer();
        if(!NarrativeCraft.getInstance().getRecordingHandler().isPlayerRecording(player)) {
            context.getSource().sendFailure(Translation.message("record.stop.fail"));
            return 0;
        }

        Recording recording = NarrativeCraft.getInstance().getRecordingHandler().getRecordingOfPlayer(context.getSource().getPlayer());
        recording.stop();

        context.getSource().sendSuccess(() -> Translation.message("record.stop.success"), true);

        return Command.SINGLE_SUCCESS;
    }
}
