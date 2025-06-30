package fr.loudo.narrativecraft.narrative.story.inkAction;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.dialog.Dialog;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;

public class KillCharacterInkAction extends InkAction {

    public KillCharacterInkAction() {
    }

    public KillCharacterInkAction(StoryHandler storyHandler) {
        super(storyHandler);
    }

    @Override
    public InkActionResult execute(String[] command) {
        if(command.length < 1) return InkActionResult.ERROR;
        name = command[1];
        CharacterStory characterStory = storyHandler.getCharacter(name);
        if(characterStory == null) return InkActionResult.ERROR;
        for(Playback playback : NarrativeCraftMod.getInstance().getPlaybackHandler().getPlaybacks()) {
            if(playback.getCharacter().getName().equals(characterStory.getName())) {
                playback.forceStop();
            }
        }
        storyHandler.removeCharacter(characterStory);
        if(storyHandler.getCurrentDialogBox() instanceof Dialog dialog) {
            if(dialog.getCharacterName().equals(characterStory.getName())) {
                storyHandler.setCurrentDialogBox(null);
            }
        }
        sendDebugDetails();
        return InkActionResult.PASS;
    }

    @Override
    void sendDebugDetails() {
        if(storyHandler.isDebugMode()) {
            Minecraft.getInstance().player.displayClientMessage(
                    Translation.message("debug.kill_character", name),
                    false
            );
        }
    }

    @Override
    public ErrorLine validate(String[] command, int line, String lineText, Scene scene) {
        if(command.length < 1) return new ErrorLine(
                line,
                scene,
                Translation.message("validation.missing_values").getString(),
                lineText
        );
        name = command[1];
        CharacterStory characterStory = NarrativeCraftMod.getInstance().getCharacterManager().getCharacter(name);
        if(characterStory == null) {
            return new ErrorLine(
                    line,
                    scene,
                    Translation.message("validation.character", name).getString(),
                    lineText
            );
        }
        return null;
    }
}
