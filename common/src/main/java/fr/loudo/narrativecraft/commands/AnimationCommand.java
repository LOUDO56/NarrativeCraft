package fr.loudo.narrativecraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class AnimationCommand {

    //TODO: manage by session
    //TODO: remove animation string name file from subscene and cutscene
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .then(Commands.literal("animation")
                        .then(Commands.literal("remove")
                                .then(Commands.argument("chapter_index", IntegerArgumentType.integer())
                                        .suggests(NarrativeCraftMod.getInstance().getChapterManager().getChapterSuggestions())
                                        .then(Commands.argument("scene_name", StringArgumentType.string())
                                                .suggests(NarrativeCraftMod.getInstance().getChapterManager().getSceneSuggestionsByChapter())
                                                .then(Commands.argument("animation_name", StringArgumentType.string())
                                                        .suggests(NarrativeCraftMod.getInstance().getChapterManager().getAnimationSuggestionByScene())
                                                        .executes(context -> {
                                                            int chapterIndex = IntegerArgumentType.getInteger(context, "chapter_index");
                                                            String sceneName = StringArgumentType.getString(context, "scene_name");
                                                            String animationName = StringArgumentType.getString(context, "animation_name");
                                                            return removeAnimation(context, chapterIndex, sceneName, animationName);
                                                        })
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int removeAnimation(CommandContext<CommandSourceStack> context, int chapterIndex, String sceneName, String animationName) {

        if(!NarrativeCraftMod.getInstance().getChapterManager().chapterExists(chapterIndex)) {
            context.getSource().sendFailure(Translation.message("chapter.no_exists", chapterIndex));
            return 0;
        }

        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getChapterByIndex(chapterIndex);

        if(!chapter.sceneExists(sceneName)) {
            context.getSource().sendFailure(Translation.message("scene.no_exists", sceneName, chapterIndex));
            return 0;
        }

        if(!NarrativeCraftFile.animationFileExists(chapterIndex, sceneName, animationName)) {
            context.getSource().sendFailure(Translation.message("animation.no_exists", animationName, sceneName, chapterIndex));
            return 0;
        }

        Scene scene = chapter.getSceneByName(sceneName);

        if(scene.removeAnimation(animationName)) {
            context.getSource().sendSuccess(() -> Translation.message("animation.delete.success", animationName, sceneName, chapterIndex), true);
        } else {
            context.getSource().sendFailure(Translation.message("animation.delete.fail", animationName, sceneName, chapterIndex));

        }

        return Command.SINGLE_SUCCESS;
    }

}
