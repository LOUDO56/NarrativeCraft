package fr.loudo.narrativecraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class SubsceneCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .then(Commands.literal("subscene")
                        .then(Commands.literal("create")
                                .then(Commands.argument("subscene_name", StringArgumentType.string())
                                        .executes(context -> {
                                            String name = StringArgumentType.getString(context, "subscene_name");
                                            return createSubscene(context, name);
                                        })
                                )
                        )
                        .then(Commands.literal("remove")
                                .then(Commands.argument("subscene_name", StringArgumentType.string())
                                        .suggests(NarrativeCraftMod.getInstance().getChapterManager().getSubsceneSuggestionByScene())
                                        .executes(context -> {
                                            String name = StringArgumentType.getString(context, "subscene_name");
                                            return removeSubscene(context, name);
                                        })
                                )
                        )
                        .then(Commands.literal("add_anim")
                                .then(Commands.argument("animation_name", StringArgumentType.string())
                                        .suggests(NarrativeCraftMod.getInstance().getChapterManager().getAnimationSuggestionBySceneFromSession())
                                        .then(Commands.literal("to")
                                                .then(Commands.argument("subscene_name", StringArgumentType.string())
                                                        .suggests(NarrativeCraftMod.getInstance().getChapterManager().getSubsceneSuggestionByScene())
                                                        .executes(context -> {
                                                            String animationName = StringArgumentType.getString(context, "animation_name");
                                                            String subsceneName = StringArgumentType.getString(context, "subscene_name");
                                                            return addAnimationToSubscene(context, animationName, subsceneName);
                                                        })
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("remove_anim")
                                .then(Commands.argument("animation_name", StringArgumentType.string())
                                        .suggests(NarrativeCraftMod.getInstance().getChapterManager().getAnimationSuggestionBySceneFromSession())
                                        .then(Commands.literal("from")
                                                .then(Commands.argument("subscene_name", StringArgumentType.string())
                                                        .suggests(NarrativeCraftMod.getInstance().getChapterManager().getSubsceneSuggestionByScene())
                                                        .executes(context -> {
                                                            String animationName = StringArgumentType.getString(context, "animation_name");
                                                            String subsceneName = StringArgumentType.getString(context, "subscene_name");
                                                            return removeAnimationFromSubscene(context, animationName, subsceneName);
                                                        })
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int createSubscene(CommandContext<CommandSourceStack> context, String subsceneName) {

        ServerPlayer player = context.getSource().getPlayer();
        PlayerSession playerSession = Utils.getSessionOrNull(player);
        if(playerSession == null) {
            context.getSource().sendFailure(Translation.message("session.not_set"));
            return 0;
        }

        if(playerSession.getScene().subsceneExists(subsceneName)) {
            context.getSource().sendFailure(Translation.message("subscene.already_exists", subsceneName, playerSession.getScene().getName(), playerSession.getChapter().getIndex()));
            return 0;
        }

        Subscene subscene = new Subscene(subsceneName, playerSession.getScene());
        if(playerSession.getScene().addSubscene(subscene)) {
            context.getSource().sendSuccess(() -> Translation.message("subscene.create.success", subsceneName, playerSession.getScene().getName(), playerSession.getChapter().getIndex()), true);
        } else {
            context.getSource().sendFailure(Translation.message("subscene.create.fail", subsceneName, playerSession.getScene().getName(), playerSession.getChapter().getIndex()));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int removeSubscene(CommandContext<CommandSourceStack> context, String subsceneName) {

        ServerPlayer player = context.getSource().getPlayer();
        PlayerSession playerSession = Utils.getSessionOrNull(player);
        if(playerSession == null) {
            context.getSource().sendFailure(Translation.message("session.not_set"));
            return 0;
        }

        if(!playerSession.getScene().subsceneExists(subsceneName)) {
            context.getSource().sendFailure(Translation.message("subscene.no_exists", subsceneName, playerSession.getScene().getName(), playerSession.getChapter().getIndex()));
            return 0;
        }

        Subscene subscene = playerSession.getScene().getSubsceneByName(subsceneName);
        if(playerSession.getScene().removeSubscene(subscene)) {
            context.getSource().sendSuccess(() -> Translation.message("subscene.delete.success", subsceneName, playerSession.getScene().getName(), playerSession.getChapter().getIndex()), true);
        } else {
            context.getSource().sendFailure(Translation.message("subscene.delete.fail", subsceneName, playerSession.getScene().getName(), playerSession.getChapter().getIndex()));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int addAnimationToSubscene(CommandContext<CommandSourceStack> context, String animationName, String subsceneName) {

        ServerPlayer player = context.getSource().getPlayer();
        PlayerSession playerSession = Utils.getSessionOrNull(player);
        if(playerSession == null) {
            context.getSource().sendFailure(Translation.message("session.not_set"));
            return 0;
        }

        if(!NarrativeCraftFile.animationFileExists(playerSession.getChapter().getIndex(), playerSession.getScene().getName(), animationName)) {
            context.getSource().sendFailure(Translation.message("animation.no_exists", animationName, playerSession.getScene().getName(), playerSession.getChapter().getIndex()));
            return 0;
        }

        if(!playerSession.getScene().subsceneExists(subsceneName)) {
            context.getSource().sendFailure(Translation.message("subscene.no_exists", subsceneName, playerSession.getScene().getName(), playerSession.getChapter().getIndex()));
            return 0;
        }

        Subscene subscene = playerSession.getScene().getSubsceneByName(subsceneName);

        if(subscene.animationExists(animationName)) {
            context.getSource().sendFailure(Translation.message("subscene.animation.already_exists", animationName, subscene.getName()));
            return 0;
        }

        if(subscene.addAnimation(animationName)) {
            context.getSource().sendSuccess(() -> Translation.message("subscene.animation.added", animationName, subscene.getName()), true);
        } else {
            context.getSource().sendFailure(Translation.message("subscene.animation.added.fail", animationName, subscene.getName()));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int removeAnimationFromSubscene(CommandContext<CommandSourceStack> context, String animationName, String subsceneName) {

        ServerPlayer player = context.getSource().getPlayer();
        PlayerSession playerSession = Utils.getSessionOrNull(player);
        if(playerSession == null) {
            context.getSource().sendFailure(Translation.message("session.not_set"));
            return 0;
        }

        if(!NarrativeCraftFile.animationFileExists(playerSession.getChapter().getIndex(), playerSession.getScene().getName(), animationName)) {
            context.getSource().sendFailure(Translation.message("animation.no_exists", animationName, playerSession.getScene().getName(), playerSession.getChapter().getIndex()));
            return 0;
        }

        if(!playerSession.getScene().subsceneExists(subsceneName)) {
            context.getSource().sendFailure(Translation.message("subscene.no_exists", subsceneName, playerSession.getScene().getName(), playerSession.getChapter().getIndex()));
            return 0;
        }

        Subscene subscene = playerSession.getScene().getSubsceneByName(subsceneName);

        if(!subscene.animationExists(animationName)) {
            context.getSource().sendFailure(Translation.message("subscene.animation.no_exists", animationName, subscene.getName()));
            return 0;
        }

        if(subscene.removeAnimation(animationName)) {
            context.getSource().sendSuccess(() -> Translation.message("subscene.animation.removed", animationName, subscene.getName()), true);
        } else {
            context.getSource().sendFailure(Translation.message("subscene.animation.removed.fail", animationName, subscene.getName()));
        }

        return Command.SINGLE_SUCCESS;
    }

}
