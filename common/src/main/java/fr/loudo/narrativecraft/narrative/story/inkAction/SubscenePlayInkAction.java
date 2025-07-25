package fr.loudo.narrativecraft.narrative.story.inkAction;

import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;
import fr.loudo.narrativecraft.narrative.dialog.Dialog;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.inkAction.enums.InkActionResult;
import fr.loudo.narrativecraft.narrative.story.inkAction.enums.InkTagType;
import fr.loudo.narrativecraft.narrative.story.inkAction.validation.ErrorLine;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class SubscenePlayInkAction extends InkAction {

    private Subscene subscene;
    private boolean isLooping, block, unique;

    public SubscenePlayInkAction(StoryHandler storyHandler, String command) {
        super(storyHandler, InkTagType.SUBSCENE, command);
    }

    @Override
    public InkActionResult execute() {
        if(command.length >= 3) {
            name = InkAction.parseName(command, 2);
            int newIndex = InkAction.getIndexFromName(command, 2);
            isLooping = false;
            try {
                if(command[newIndex + 1].equals("true") || command[newIndex + 1].equals("false")) {
                    isLooping = Boolean.parseBoolean(command[newIndex + 1]);
                }
            } catch (RuntimeException ignored) {}
            subscene = storyHandler.getPlayerSession().getScene().getSubsceneByName(name);
            if(subscene == null) return InkActionResult.ERROR;
            block = false;
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
                subscene.start(Utils.getServerLevel(), Playback.PlaybackType.PRODUCTION, isLooping);
                for(Playback playback : subscene.getPlaybackList()) {
                    playback.setUnique(unique);
                }
                storyHandler.getInkActionList().add(this);
                sendDebugDetails();
            } else if(command[1].equals("stop")) {
                List<InkAction> toRemove = new ArrayList<>();
                for (InkAction inkAction : storyHandler.getInkActionList()) {
                    if (inkAction instanceof SubscenePlayInkAction action) {
                        if(action.subscene.getPlaybackList().equals(subscene.getPlaybackList())) {
                            for(Animation animation : subscene.getAnimationList()) {
                                if(storyHandler.getCurrentDialogBox() instanceof Dialog dialog) {
                                    if(dialog.getEntityClient().getUUID().equals(animation.getCharacter().getEntity().getUUID())) {
                                        storyHandler.setCurrentDialogBox(null);
                                    }
                                }
                                storyHandler.getCurrentCharacters().remove(animation.getCharacter());
                            }
                            subscene.forceStop();
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
            Minecraft.getInstance().player.displayClientMessage(Translation.message("debug.subscene", name, isLooping), false);
        }
    }

    @Override
    public ErrorLine validate(String[] command, int line, String lineText, Scene scene) {
        if(command.length < 3) {
            return new ErrorLine(
                    line,
                    scene,
                    Translation.message("validation.missing_name").getString(),
                    lineText, false
            );
        }
        name = InkAction.parseName(command, 2);
        Subscene subscene1 = scene.getSubsceneByName(name);
        if(subscene1 == null) {
            return new ErrorLine(
                    line,
                    scene,
                    Translation.message("validation.subscene", name).getString(),
                    lineText, false
            );
        }
        return null;
    }

    public boolean isLooping() {
        return isLooping;
    }

    public Subscene getSubscene() {
        return subscene;
    }

    public boolean isBlock() {
        return block;
    }
}
