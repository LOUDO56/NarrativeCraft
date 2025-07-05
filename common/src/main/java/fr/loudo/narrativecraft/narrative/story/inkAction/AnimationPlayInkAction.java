package fr.loudo.narrativecraft.narrative.story.inkAction;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.dialog.Dialog;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class AnimationPlayInkAction extends InkAction {

    private Animation animation;
    private Playback playback;
    private boolean isLooping, block;

    public AnimationPlayInkAction() {}

    public AnimationPlayInkAction(StoryHandler storyHandler) {
        super(storyHandler, InkTagType.ANIMATION);
    }

    public AnimationPlayInkAction(StoryHandler storyHandler, Animation animation, Playback playback) {
        super(storyHandler, InkTagType.ANIMATION);
        this.animation = animation;
        this.playback = playback;
    }

    @Override
    public InkActionResult execute(String[] command) {
        if(command.length >= 3) {
            name = InkAction.parseName(command, 2);
            isLooping = false;
            try {
                int boolIndex = command.length - 1;
                if(command.length > 4) {
                    boolIndex--;
                }
                if(command[boolIndex].equals("true") || command[boolIndex].equals("false")) {
                    isLooping = Boolean.parseBoolean(command[boolIndex]);
                }
            } catch (RuntimeException ignored) {}
            animation = storyHandler.getPlayerSession().getScene().getAnimationByName(name);
            block = false;
            PlayerSession playerSession = storyHandler.getPlayerSession();
            if(animation == null) return InkActionResult.ERROR;
            if(command[1].equals("start")) {
                try {
                    if(command[command.length - 1].equals("block")) block = true;
                } catch (RuntimeException ignored) {}
                storyHandler.addCharacter(animation.getCharacter());
                playback = new Playback(
                        animation,
                        playerSession.getPlayer().serverLevel(),
                        animation.getCharacter(),
                        Playback.PlaybackType.PRODUCTION,
                        isLooping
                );
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
            return InkActionResult.BLOCK;
        } else {
            return InkActionResult.PASS;
        }
    }

    @Override
    void sendDebugDetails() {
        if(storyHandler.isDebugMode()) {
            Minecraft.getInstance().player.displayClientMessage(Translation.message("debug.animation", name), false);
        }
    }

    @Override
    public ErrorLine validate(String[] command, int line, String lineText, Scene scene) {
        if(command.length < 3) {
            return new ErrorLine(
                    line,
                    scene,
                    Translation.message("validation.missing_name").getString(),
                    lineText
            );
        }
        name = InkAction.parseName(command, 2);
        Animation animation1 = scene.getAnimationByName(name);
        if(animation1 == null) {
            return new ErrorLine(
                    line,
                    scene,
                    Translation.message("validation.animation", name).getString(),
                    lineText
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
