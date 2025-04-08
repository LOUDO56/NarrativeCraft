package fr.loudo.narrativecraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.cutscenes.Cutscene;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;

public class CutsceneCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .then(Commands.literal("cutscene")
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
}
