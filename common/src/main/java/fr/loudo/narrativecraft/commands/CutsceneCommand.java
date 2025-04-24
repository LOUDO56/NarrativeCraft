package fr.loudo.narrativecraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.Cutscene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class CutsceneCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .then(Commands.literal("cutscene")
                        .then(Commands.literal("manager")
                                .then(Commands.literal("create")
                                        .then(Commands.argument("cutscene_name", StringArgumentType.string())
                                                .executes(context -> {
                                                    String cutsceneName = StringArgumentType.getString(context, "cutscene_name");
                                                    return createCutscene(context, cutsceneName);
                                                })
                                        )
                                )
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("cutscene_name", StringArgumentType.string())
                                                .suggests(NarrativeCraftMod.getInstance().getChapterManager().getCutsceneSuggestionBySceneFromSession())
                                                .executes(context -> {
                                                    String cutsceneName = StringArgumentType.getString(context, "cutscene_name");
                                                    return removeCutscene(context, cutsceneName);
                                                })
                                        )
                                )
                                .then(Commands.literal("addSubscene")
                                        .then(Commands.argument("subscene_name", StringArgumentType.string())
                                                .suggests(NarrativeCraftMod.getInstance().getChapterManager().getSubsceneSuggestionByScene())
                                                .then(Commands.literal("to")
                                                        .then(Commands.argument("cutscene_name", StringArgumentType.string())
                                                                .suggests(NarrativeCraftMod.getInstance().getChapterManager().getCutsceneSuggestionBySceneFromSession())
                                                                .executes(context -> {
                                                                    String subsceneName = StringArgumentType.getString(context, "subscene_name");
                                                                    String cutsceneName = StringArgumentType.getString(context, "cutscene_name");
                                                                    return addSubsceneToCutscene(context, subsceneName, cutsceneName);
                                                                })
                                                        )
                                                )
                                        )
                                )
                                .then(Commands.literal("removeSubscene")
                                        .then(Commands.argument("subscene_name", StringArgumentType.string())
                                                .suggests(NarrativeCraftMod.getInstance().getChapterManager().getSubsceneSuggestionByScene())
                                                .then(Commands.literal("from")
                                                        .then(Commands.argument("cutscene_name", StringArgumentType.string())
                                                                .suggests(NarrativeCraftMod.getInstance().getChapterManager().getCutsceneSuggestionBySceneFromSession())
                                                                .executes(context -> {
                                                                    String subsceneName = StringArgumentType.getString(context, "subscene_name");
                                                                    String cutsceneName = StringArgumentType.getString(context, "cutscene_name");
                                                                    return removeSubsceneFromCutscene(context, subsceneName, cutsceneName);
                                                                })
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("playback")
                                .then(Commands.literal("edit")
                                        .then(Commands.argument("cutscene_name", StringArgumentType.string())
                                                .suggests(NarrativeCraftMod.getInstance().getChapterManager().getCutsceneSuggestionBySceneFromSession())
                                                .executes(context -> {
                                                    String cutsceneName = StringArgumentType.getString(context, "cutscene_name");
                                                    return editCutscene(context, cutsceneName);
                                                })
                                        )
                                )
                                .then(Commands.literal("changeTime")
                                        .then(Commands.argument("tick", IntegerArgumentType.integer())
                                                .executes(context -> {
                                                    int tick = IntegerArgumentType.getInteger(context, "tick");
                                                    return changeTimeTickCutscene(context, tick);
                                                })
                                        )
                                )
                                .then(Commands.literal("resume")
                                        .executes(CutsceneCommand::resumeCutscene)
                                )
                                .then(Commands.literal("pause")
                                        .executes(CutsceneCommand::pauseCutscene)
                                )
                                .then(Commands.literal("leave")
                                        .executes(CutsceneCommand::leaveCutscene)
                                )
                        )
                )
        );
    }

    private static int createCutscene(CommandContext<CommandSourceStack> context, String cutsceneName) {

        ServerPlayer player = context.getSource().getPlayer();
        PlayerSession playerSession = Utils.getSessionOrNull(player);
        if(playerSession == null) {
            context.getSource().sendFailure(Translation.message("session.not_set"));
            return 0;
        }

        if(NarrativeCraftFile.cutsceneFileExists(playerSession.getChapter().getIndex(), playerSession.getScene().getName(), cutsceneName)) {
            context.getSource().sendFailure(Translation.message("cutscene.already_exists", cutsceneName, playerSession.getScene().getName()));
            return 0;
        }

        if(playerSession.getScene().addCutscene(cutsceneName)) {
            context.getSource().sendSuccess(() -> Translation.message("cutscene.create.success", cutsceneName, playerSession.getScene().getName()), true);
        } else {
            context.getSource().sendFailure(Translation.message("cutscene.create.fail", cutsceneName, playerSession.getScene().getName()));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int removeCutscene(CommandContext<CommandSourceStack> context, String cutsceneName) {

        ServerPlayer player = context.getSource().getPlayer();
        PlayerSession playerSession = Utils.getSessionOrNull(player);
        if(playerSession == null) {
            context.getSource().sendFailure(Translation.message("session.not_set"));
            return 0;
        }

        if(!NarrativeCraftFile.cutsceneFileExists(playerSession.getChapter().getIndex(), playerSession.getScene().getName(), cutsceneName)) {
            context.getSource().sendFailure(Translation.message("cutscene.no_exists", cutsceneName, playerSession.getScene().getName()));
            return 0;
        }

        if(playerSession.getScene().removeCutscene(cutsceneName)) {
            context.getSource().sendSuccess(() -> Translation.message("cutscene.delete.success", cutsceneName, playerSession.getScene().getName()), true);
        } else {
            context.getSource().sendFailure(Translation.message("cutscene.delete.fail", cutsceneName, playerSession.getScene().getName()));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int addSubsceneToCutscene(CommandContext<CommandSourceStack> context, String subsceneName, String cutsceneName) {

        ServerPlayer player = context.getSource().getPlayer();
        PlayerSession playerSession = Utils.getSessionOrNull(player);
        if(playerSession == null) {
            context.getSource().sendFailure(Translation.message("session.not_set"));
            return 0;
        }

        if(!NarrativeCraftFile.cutsceneFileExists(playerSession.getChapter().getIndex(), playerSession.getScene().getName(), cutsceneName)) {
            context.getSource().sendFailure(Translation.message("cutscene.no_exists", cutsceneName, playerSession.getScene().getName()));
            return 0;
        }

        if(!playerSession.getScene().subsceneExists(subsceneName)) {
            context.getSource().sendFailure(Translation.message("subscene.no_exists", subsceneName, playerSession.getScene().getName(), playerSession.getChapter().getIndex()));
            return 0;
        }

        Cutscene cutscene = NarrativeCraftFile.getCutsceneFromFile(playerSession.getChapter().getIndex(), playerSession.getScene().getName(), cutsceneName);
        Subscene subscene = playerSession.getScene().getSubsceneByName(subsceneName);

        if(cutscene.subsceneExists(subsceneName)) {
            context.getSource().sendFailure(Translation.message("cutscene.subscene.already_exists", subsceneName, cutscene.getName(), playerSession.getScene().getName()));
            return 0;
        }

        if(cutscene.addSubscene(subscene)) {
            context.getSource().sendSuccess(() -> Translation.message("cutscene.subscene.added", subscene.getName(), cutsceneName, playerSession.getScene().getName()), false);
        } else {
            context.getSource().sendFailure(Translation.message("cutscene.subscene.added.fail", subscene.getName(), cutscene.getName(), playerSession.getScene().getName(), cutscene.getName()));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int removeSubsceneFromCutscene(CommandContext<CommandSourceStack> context, String subsceneName, String cutsceneName) {

        ServerPlayer player = context.getSource().getPlayer();
        PlayerSession playerSession = Utils.getSessionOrNull(player);
        if(playerSession == null) {
            context.getSource().sendFailure(Translation.message("session.not_set"));
            return 0;
        }

        if(!NarrativeCraftFile.cutsceneFileExists(playerSession.getChapter().getIndex(), playerSession.getScene().getName(), cutsceneName)) {
            context.getSource().sendFailure(Translation.message("cutscene.no_exists", cutsceneName, playerSession.getScene().getName()));
            return 0;
        }

        if(!playerSession.getScene().subsceneExists(subsceneName)) {
            context.getSource().sendFailure(Translation.message("subscene.no_exists", subsceneName, playerSession.getScene().getName(), playerSession.getChapter().getIndex()));
            return 0;
        }

        Cutscene cutscene = NarrativeCraftFile.getCutsceneFromFile(playerSession.getChapter().getIndex(), playerSession.getScene().getName(), cutsceneName);
        Subscene subscene = cutscene.getSubsceneByName(subsceneName);

        if(!cutscene.subsceneExists(subsceneName)) {
            context.getSource().sendFailure(Translation.message("cutscene.subscene.no_exists", subsceneName, cutscene.getName(), playerSession.getScene().getName()));
            return 0;
        }

        if(cutscene.removeSubscene(subscene)) {
            context.getSource().sendSuccess(() -> Translation.message("cutscene.subscene.removed", subscene.getName(), cutscene.getName(), playerSession.getScene().getName()), false);
        } else {
            context.getSource().sendFailure(Translation.message("cutscene.subscene.removed.fail", subsceneName, cutscene.getName(), playerSession.getScene().getName(), cutscene.getName()));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int editCutscene(CommandContext<CommandSourceStack> context, String cutsceneName) {

        ServerPlayer player = context.getSource().getPlayer();
        PlayerSession playerSession = Utils.getSessionOrNull(player);
        if (playerSession == null) {
            context.getSource().sendFailure(Translation.message("session.not_set"));
            return 0;
        }

        if (!NarrativeCraftFile.cutsceneFileExists(playerSession.getChapter().getIndex(), playerSession.getScene().getName(), cutsceneName)) {
            context.getSource().sendFailure(Translation.message("cutscene.no_exists", cutsceneName, playerSession.getScene().getName()));
            return 0;
        }

        Cutscene cutscene = NarrativeCraftFile.getCutsceneFromFile(playerSession.getChapter().getIndex(), playerSession.getScene().getName(), cutsceneName);
        CutsceneController cutsceneController = new CutsceneController(cutscene, player);
        playerSession.setCutsceneController(cutsceneController);
        cutsceneController.startSession();
        context.getSource().sendSuccess(() -> Translation.message("cutscene.edit.session_set", cutsceneName, playerSession.getScene().getName()), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int resumeCutscene(CommandContext<CommandSourceStack> context) {

        ServerPlayer player = context.getSource().getPlayer();
        PlayerSession playerSession = Utils.getSessionOrNull(player);
        if (playerSession == null) {
            context.getSource().sendFailure(Translation.message("session.not_set"));
            return 0;
        }
        CutsceneController cutsceneController = playerSession.getCutsceneController();

        if (cutsceneController == null) {
            context.getSource().sendFailure(Translation.message("cutscene.edit.no_session"));
            return 0;
        }

        cutsceneController.resume();
        context.getSource().sendSuccess(() -> Translation.message("cutscene.edit.resume"), false);


        return Command.SINGLE_SUCCESS;
    }

    private static int pauseCutscene(CommandContext<CommandSourceStack> context) {

        ServerPlayer player = context.getSource().getPlayer();
        PlayerSession playerSession = Utils.getSessionOrNull(player);
        if (playerSession == null) {
            context.getSource().sendFailure(Translation.message("session.not_set"));
            return 0;
        }
        CutsceneController cutsceneController = playerSession.getCutsceneController();

        if (cutsceneController == null) {
            context.getSource().sendFailure(Translation.message("cutscene.edit.no_session"));
            return 0;
        }

        cutsceneController.pause();
        context.getSource().sendSuccess(() -> Translation.message("cutscene.edit.pause"), false);


        return Command.SINGLE_SUCCESS;
    }

    private static int changeTimeTickCutscene(CommandContext<CommandSourceStack> context, int tick) {

        ServerPlayer player = context.getSource().getPlayer();
        PlayerSession playerSession = Utils.getSessionOrNull(player);
        if (playerSession == null) {
            context.getSource().sendFailure(Translation.message("session.not_set"));
            return 0;
        }
        CutsceneController cutsceneController = playerSession.getCutsceneController();

        if (cutsceneController == null) {
            context.getSource().sendFailure(Translation.message("cutscene.edit.no_session"));
            return 0;
        }

        cutsceneController.changeTimePosition(tick, false);
        context.getSource().sendSuccess(() -> Translation.message("cutscene.edit.changed_time", tick), false);


        return Command.SINGLE_SUCCESS;
    }

    private static int leaveCutscene(CommandContext<CommandSourceStack> context) {

        ServerPlayer player = context.getSource().getPlayer();
        PlayerSession playerSession = Utils.getSessionOrNull(player);
        if (playerSession == null) {
            context.getSource().sendFailure(Translation.message("session.not_set"));
            return 0;
        }
        CutsceneController cutsceneController = playerSession.getCutsceneController();

        if (cutsceneController == null) {
            context.getSource().sendFailure(Translation.message("cutscene.edit.no_session"));
            return 0;
        }

        cutsceneController.stopSession();
        playerSession.setCutsceneController(null);
        context.getSource().sendSuccess(() -> Translation.message("cutscene.edit.session_left", cutsceneController.getCutscene().getName(), playerSession.getScene().getName()), false);



        return Command.SINGLE_SUCCESS;
    }
}
