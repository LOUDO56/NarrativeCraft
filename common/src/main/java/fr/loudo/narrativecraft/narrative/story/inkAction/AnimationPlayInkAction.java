package fr.loudo.narrativecraft.narrative.story.inkAction;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.dialog.Dialog;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.inkAction.InkActionResult;
import fr.loudo.narrativecraft.narrative.story.inkAction.enums.InkTagType;
import fr.loudo.narrativecraft.narrative.story.inkAction.validation.ErrorLine;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class AnimationPlayInkAction extends InkAction {

    private Animation animation;
    private Playback playback;
    private boolean isLooping, block, unique;

    public AnimationPlayInkAction(StoryHandler storyHandler, String command) {
        super(storyHandler, InkTagType.ANIMATION, command);
    }

    @Override
    public InkActionResult execute() {
        if(command.length >= 3) {
            name = InkAction.parseName(command, 2);
            int newIndex = InkAction.getNewIndexFromName(command, 2);
            isLooping = false;
            try {
                if(command[newIndex + 1].equals("true") || command[newIndex + 1].equals("false")) {
                    isLooping = Boolean.parseBoolean(command[newIndex + 1]);
                }
            } catch (RuntimeException ignored) {}
            animation = storyHandler.getPlayerSession().getScene().getAnimationByName(name);
            block = false;
            unique = false;
            if(animation == null) return InkActionResult.error(this.getClass(), Translation.message("validation.animation", name).getString());
            if(command[1].equals("start")) {
                try {
                    if(command[newIndex + 2].equals("true") || command[newIndex + 2].equals("false")) {
                        unique = Boolean.parseBoolean(command[newIndex + 2]);
                    }
                } catch (RuntimeException ignored) {}
                try {
                    if(command[newIndex + 3].equals("true") || command[newIndex + 3].equals("false")) {
                        block = Boolean.parseBoolean(command[newIndex + 3]);
                    }
                } catch (RuntimeException ignored) {}
                playback = new Playback(
                        animation,
                        Utils.getServerLevel(),
                        animation.getCharacter(),
                        Playback.PlaybackType.PRODUCTION,
                        isLooping
                );
                playback.setUnique(unique);
                playback.start();
                storyHandler.getInkActionList().add(this);
                sendDebugDetails();
            } else if(command[1].equals("stop")) {
                List<InkAction> toRemove = new ArrayList<>();
                for(InkAction inkAction : storyHandler.getInkActionList()) {
                    if(inkAction instanceof AnimationPlayInkAction action) {
                        if(animation.getName().equals(action.animation.getName())) {
                            if(storyHandler.getCurrentDialogBox() instanceof Dialog dialog) {
                                if(dialog.getEntityClient().getUUID().equals(action.playback.getMasterEntity().getUUID())) {
                                    storyHandler.setCurrentDialogBox(null);
                                }
                            }
                            storyHandler.getCurrentCharacters().remove(animation.getCharacter());
                            action.playback.forceStop();
                            NarrativeCraftMod.getInstance().getPlaybackHandler().removePlayback(action.playback);
                            toRemove.add(action);
                        }
                    }
                }
                storyHandler.getInkActionList().removeAll(toRemove);
            }
        }
        if(block) {
            return InkActionResult.block();
        } else {
            return InkActionResult.pass();
        }
    }

    @Override
    void sendDebugDetails() {
        if(storyHandler.isDebugMode()) {
            Minecraft.getInstance().player.displayClientMessage(Translation.message("debug.animation", name, isLooping), false);
        }
    }

    @Override
    public ErrorLine validate(String[] command, int line, String lineText, Scene scene) {
        if(command.length < 3) {
            return new ErrorLine(
                    line,
                    scene,
                    Translation.message("validation.missing_name").getString(),
                    lineText,
                    false
            );
        }
        name = InkAction.parseName(command, 2);
        Animation animation1 = scene.getAnimationByName(name);
        if(animation1 == null) {
            return new ErrorLine(
                    line,
                    scene,
                    Translation.message("validation.animation", name).getString(),
                    lineText,
                    false
            );
        }
        return null;
    }

    public Playback getPlayback() {
        return playback;
    }

    public boolean isLooping() {
        return isLooping;
    }

    public boolean isBlock() {
        return block;
    }
}
