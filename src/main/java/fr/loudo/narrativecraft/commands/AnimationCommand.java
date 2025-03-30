package fr.loudo.narrativecraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.loudo.narrativecraft.NarrativeCraft;
import fr.loudo.narrativecraft.narrative.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.scenes.Scene;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class AnimationCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc")
                .then(Commands.literal("animation")
                        .then(Commands.literal("create")
                                .then(Commands.argument("chapter_index", IntegerArgumentType.integer())
                                        .suggests(NarrativeCraft.getChapterManager().getChapterSuggestions())
                                        .then(Commands.argument("scene_name", StringArgumentType.string())
                                                .suggests(NarrativeCraft.getChapterManager().getSceneSuggestionsByChapter())
                                                .then(Commands.argument("animation_name", StringArgumentType.string())
                                                        .executes(context -> {
                                                            int chapterIndex = IntegerArgumentType.getInteger(context, "chapter_index");
                                                            String sceneName = StringArgumentType.getString(context, "scene_name");
                                                            String animationName = StringArgumentType.getString(context, "animation_name");
                                                            return createAnimation(context, chapterIndex, sceneName, animationName);
                                                        })
                                                )
                                        )
                                )
                        )
                )
        );
    }


    private static int createAnimation(CommandContext<CommandSourceStack> context, int chapterIndex, String sceneName, String animationName) {

        if(!NarrativeCraft.getChapterManager().chapterExists(chapterIndex)) {
            context.getSource().sendFailure(Translation.message("chapter.no_exists", chapterIndex));
            return 0;
        }

        Chapter chapter = NarrativeCraft.getChapterManager().getChapterByIndex(chapterIndex);

        if(!chapter.sceneExists(sceneName)) {
            context.getSource().sendFailure(Translation.message("scene.no_exists", sceneName, chapterIndex));
            return 0;
        }

        Scene scene = chapter.getSceneByName(sceneName);

        if(scene.animationExists(animationName)) {
            Animation animation = scene.getAnimationByName(animationName);
            context.getSource().sendFailure(Translation.message("animation.already_exists", animation.getName(), scene.getName(), chapterIndex));
            return 0;
        }

        Animation animation = new Animation(scene, animationName);
        if(scene.addAnimation(animation)) {
            context.getSource().sendSuccess(() -> Translation.message("animation.create.success", animation.getName(), scene.getName(), chapterIndex), true);
        } else {
            context.getSource().sendSuccess(() -> Translation.message("animation.create.fail", animation.getName(), scene.getName(), chapterIndex), true);
        }
        return Command.SINGLE_SUCCESS;
    }

}
