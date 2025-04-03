package fr.loudo.narrativecraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.loudo.narrativecraft.NarrativeCraftManager;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.animations.Animation;
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
                        .then(Commands.literal("save")
                                .then(Commands.argument("animation_name", StringArgumentType.string())
                                        .executes(context -> saveRecording(context, StringArgumentType.getString(context, "animation_name")))
                                )
                        )
                )
        );
    }

    private static int startRecording(CommandContext<CommandSourceStack> context) {

        ServerPlayer player = context.getSource().getPlayer();
        PlayerSession playerSession = NarrativeCraftManager.getInstance().getPlayerSessionManager().getPlayerSession(player);
        if(playerSession.getChapter() == null || playerSession.getScene() == null) {
            context.getSource().sendFailure(Translation.message("record.start.no_session"));
            return 0;
        }

        if(NarrativeCraftManager.getInstance().getRecordingHandler().isPlayerRecording(player)) {
            context.getSource().sendFailure(Translation.message("record.start.already_recording"));
            return 0;
        }

        Recording recording = new Recording(context.getSource().getPlayer());
        recording.start();

        context.getSource().sendSuccess(() -> Translation.message("record.start.success"), true);

        return Command.SINGLE_SUCCESS;
    }


    private static int stopRecording(CommandContext<CommandSourceStack> context) {

        ServerPlayer player = context.getSource().getPlayer();
        if(!NarrativeCraftManager.getInstance().getRecordingHandler().isPlayerRecording(player)) {
            context.getSource().sendFailure(Translation.message("record.stop.no_recording"));
            return 0;
        }

        Recording recording = NarrativeCraftManager.getInstance().getRecordingHandler().getRecordingOfPlayer(context.getSource().getPlayer());
        if(!recording.isRecording()) {
            context.getSource().sendFailure(Translation.message("record.stop.no_recording"));
            return 0;
        }
        recording.stop();

        context.getSource().sendSuccess(() -> Translation.message("record.stop.success"), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int saveRecording(CommandContext<CommandSourceStack> context, String newAnimationName) {
        ServerPlayer player = context.getSource().getPlayer();

        Recording recording = NarrativeCraftManager.getInstance().getRecordingHandler().getRecordingOfPlayer(context.getSource().getPlayer());
        if(recording == null) {
            context.getSource().sendFailure(Translation.message("record.save.recorded_nothing"));
            return 0;
        }

        PlayerSession playerSession = NarrativeCraftManager.getInstance().getPlayerSessionManager().getPlayerSession(player);

        Animation animation;
        if (NarrativeCraftFile.animationFileExists(newAnimationName)) {
            if (playerSession.isOverwriteState()) {
                animation = NarrativeCraftFile.getAnimationFromFile(newAnimationName);
                playerSession.setOverwriteState(false);
            } else {
                context.getSource().sendFailure(Translation.message("record.save.overwrite", newAnimationName, playerSession.getChapter().getIndex(), playerSession.getScene().getName()));
                playerSession.setOverwriteState(true);
                return 0;
            }
        } else {
            animation = new Animation(playerSession.getScene(), newAnimationName);
            playerSession.setOverwriteState(false);
        }

        if (recording.save(animation)) {
            context.getSource().sendSuccess(() -> Translation.message("record.save.success", animation.getName(), playerSession.getChapter().getIndex(), playerSession.getScene().getName()), true);
        } else {
            context.getSource().sendFailure(Translation.message("record.save.fail", animation.getName(), playerSession.getChapter().getIndex(), playerSession.getScene().getName()));
        }

        return Command.SINGLE_SUCCESS;

    }
}
