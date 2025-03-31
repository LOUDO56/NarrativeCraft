package fr.loudo.narrativecraft.utils;

import com.mojang.brigadier.context.CommandContext;
import fr.loudo.narrativecraft.NarrativeCraft;
import fr.loudo.narrativecraft.narrative.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.ChapterManager;
import fr.loudo.narrativecraft.narrative.scenes.Scene;
import net.minecraft.commands.CommandSourceStack;

public class AnimationValidator {

    public static boolean validateAnimationContext(CommandContext<CommandSourceStack> context, int chapterIndex, String sceneName, String animationName) {
        ChapterManager chapterManager = NarrativeCraft.getInstance().getChapterManager();

        if (!chapterManager.chapterExists(chapterIndex)) {
            context.getSource().sendFailure(Translation.message("chapter.no_exists", chapterIndex));
            return false;
        }

        Chapter chapter = chapterManager.getChapterByIndex(chapterIndex);

        if (!chapter.sceneExists(sceneName)) {
            context.getSource().sendFailure(Translation.message("scene.no_exists", sceneName, chapterIndex));
            return false;
        }

        Scene scene = chapter.getSceneByName(sceneName);

        if (!scene.animationExists(animationName)) {
            context.getSource().sendFailure(Translation.message("animation.no_exists", animationName, scene.getName(), chapterIndex));
            return false;
        }

        return true;
    }
}
