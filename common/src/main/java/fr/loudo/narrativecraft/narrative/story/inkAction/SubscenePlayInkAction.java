package fr.loudo.narrativecraft.narrative.story.inkAction;

import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class SubscenePlayInkAction extends InkAction {

    private Subscene subscene;
    private boolean isLooping;

    public SubscenePlayInkAction() {}

    public SubscenePlayInkAction(StoryHandler storyHandler) {
        super(storyHandler);
    }

    public SubscenePlayInkAction(StoryHandler storyHandler, Subscene subscene) {
        super(storyHandler);
        this.subscene = subscene;
    }

    @Override
    public InkActionResult execute(String[] command) {
        if(command.length >= 3) {
            name = InkAction.parseName(command, 2);
            isLooping = false;
            if(command.length >= 4) {
                if(command[3].equals("true") || command[3].equals("false")) {
                    isLooping = Boolean.parseBoolean(command[3]);
                }
            }
            subscene = storyHandler.getPlayerSession().getScene().getSubsceneByName(name);
            if(subscene == null) return InkActionResult.ERROR;
            if(command[1].equals("start")) {
                subscene.start(storyHandler.getPlayerSession().getPlayer().serverLevel(), Playback.PlaybackType.PRODUCTION, isLooping);
                for(Animation animation : subscene.getAnimationList()) {
                    storyHandler.getCurrentCharacters().add(animation.getCharacter());
                }
                storyHandler.getInkActionList().add(this);
                sendDebugDetails();
            } else if(command[1].equals("stop")) {
                List<InkAction> toRemove = new ArrayList<>();
                for (InkAction inkAction : storyHandler.getInkActionList()) {
                    if (inkAction instanceof SubscenePlayInkAction action) {
                        if(action.subscene.getPlaybackList().equals(subscene.getPlaybackList())) {
                            for(Animation animation : subscene.getAnimationList()) {
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
        return InkActionResult.PASS;
    }

    @Override
    void sendDebugDetails() {
        if(storyHandler.isDebugMode()) {
            Minecraft.getInstance().player.displayClientMessage(Translation.message("debug.subscene", name), false);
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
        Subscene subscene1 = scene.getSubsceneByName(name);
        if(subscene1 == null) {
            return new ErrorLine(
                    line,
                    scene,
                    Translation.message("validation.subscene", name).getString(),
                    lineText
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
}
