package fr.loudo.narrativecraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecordCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .then(Commands.literal("record")
                        .then(Commands.literal("start")
                                .then(Commands.literal("with")
                                        .then(Commands.argument("subscenes", StringArgumentType.greedyString())
                                                .executes(commandContext -> {
                                                    String subscenes = StringArgumentType.getString(commandContext, "subscenes");
                                                    return startRecordingWithSubscenes(commandContext, subscenes);
                                                })
                                        )
                                )
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
        PlayerSession playerSession = Utils.getSessionOrNull(player);
        if(playerSession == null) {
            context.getSource().sendFailure(Translation.message("session.not_set"));
            return 0;
        }

        for(Subscene subscene : playerSession.getSubscenesPlaying()) {
            subscene.stop();
        }

        if(NarrativeCraftMod.getInstance().getRecordingHandler().isPlayerRecording(player)) {
            context.getSource().sendFailure(Translation.message("record.start.already_recording"));
            return 0;
        }

        Recording recording = NarrativeCraftMod.getInstance().getRecordingHandler().getRecordingOfPlayer(player);
        if(recording == null) {
            recording = new Recording(context.getSource().getPlayer());
        }
        recording.start();

        context.getSource().sendSuccess(() -> Translation.message("record.start.success"), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int startRecordingWithSubscenes(CommandContext<CommandSourceStack> context, String subscenes) {

        ServerPlayer player = context.getSource().getPlayer();
        PlayerSession playerSession = Utils.getSessionOrNull(player);
        if(playerSession == null) {
            context.getSource().sendFailure(Translation.message("session.not_set"));
            return 0;
        }

        if(NarrativeCraftMod.getInstance().getRecordingHandler().isPlayerRecording(player)) {
            context.getSource().sendFailure(Translation.message("record.start.already_recording"));
            return 0;
        }

        List<Subscene> subsceneToPlay = new ArrayList<>();
        String[] subsceneNameList = subscenes.split(",");
        for(String subsceneName : subsceneNameList) {
            Subscene subscene = playerSession.getScene().getSubsceneByName(subsceneName);
            if(subscene != null) {
                subsceneToPlay.add(subscene);
            } else {
                context.getSource().sendFailure(Translation.message("subscene.record.no_exists", subsceneName));
            }
        }

        if(subsceneNameList.length == subsceneToPlay.size()) {
            startRecording(context);
            for(Subscene subscene : subsceneToPlay) {
                subscene.start(player);
                playerSession.getSubscenesPlaying().add(subscene);
            }
            context.getSource().sendSuccess(() -> Translation.message("subscene.record.start", Arrays.toString(subsceneNameList)), true);
        }


        return Command.SINGLE_SUCCESS;
    }


    private static int stopRecording(CommandContext<CommandSourceStack> context) {

        ServerPlayer player = context.getSource().getPlayer();
        PlayerSession playerSession = Utils.getSessionOrNull(player);
        if(playerSession == null) {
            context.getSource().sendFailure(Translation.message("session.not_set"));
            return 0;
        }

        if(!NarrativeCraftMod.getInstance().getRecordingHandler().isPlayerRecording(player)) {
            context.getSource().sendFailure(Translation.message("record.stop.no_recording"));
            return 0;
        }

        for(Subscene subscene : playerSession.getSubscenesPlaying()) {
            subscene.stop();
        }

        Recording recording = NarrativeCraftMod.getInstance().getRecordingHandler().getRecordingOfPlayer(player);
        recording.stop();

        context.getSource().sendSuccess(() -> Translation.message("record.stop.success"), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int saveRecording(CommandContext<CommandSourceStack> context, String newAnimationName) {
        ServerPlayer player = context.getSource().getPlayer();
        PlayerSession playerSession = Utils.getSessionOrNull(player);
        if(playerSession == null) {
            context.getSource().sendFailure(Translation.message("session.not_set"));
            return 0;
        }

        Recording recording = NarrativeCraftMod.getInstance().getRecordingHandler().getRecordingOfPlayer(context.getSource().getPlayer());
        if(recording == null) {
            context.getSource().sendFailure(Translation.message("record.save.recorded_nothing"));
            return 0;
        }

        if(recording.isRecording()) {
            context.getSource().sendFailure(Translation.message("record.save.stop_record_before_save"));
            return 0;
        }

        recording.stop();

        Animation animation;
        if (NarrativeCraftFile.animationFileExists(playerSession.getChapter().getIndex(), playerSession.getScene().getName(), newAnimationName)) {
            if (playerSession.isOverwriteState()) {
                animation = NarrativeCraftFile.getAnimationFromFile(playerSession.getChapter().getIndex(), playerSession.getScene().getName(), newAnimationName);
                playerSession.setOverwriteState(false);
            } else {
                context.getSource().sendFailure(Translation.message("record.save.overwrite", newAnimationName, playerSession.getScene().getName(), playerSession.getChapter().getIndex()));
                playerSession.setOverwriteState(true);
                return 0;
            }
        } else {
            animation = new Animation(playerSession.getScene(), newAnimationName);
            playerSession.setOverwriteState(false);
        }

        if (recording.save(animation)) {
            context.getSource().sendSuccess(() -> Translation.message("record.save.success", playerSession.getChapter().getIndex(), animation.getName(), playerSession.getScene().getName()), true);
        } else {
            context.getSource().sendFailure(Translation.message("record.save.fail", animation.getName(), playerSession.getChapter().getIndex(), playerSession.getScene().getName()));
        }

        return Command.SINGLE_SUCCESS;

    }
}
