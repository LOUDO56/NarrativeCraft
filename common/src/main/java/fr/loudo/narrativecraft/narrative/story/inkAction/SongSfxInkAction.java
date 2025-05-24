package fr.loudo.narrativecraft.narrative.story.inkAction;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class SongSfxInkAction extends InkAction {

    private boolean loop;
    private float volume, pitch;
    private final SoundType soundType;

    public SongSfxInkAction(StoryHandler storyHandler, SoundType soundType) {
        super(storyHandler);
        this.soundType = soundType;
    }

    @Override
    public boolean execute(String[] command) {
        name = command[2];
        loop = false;
        volume = 1.0F;
        pitch = 1.0F;
        if(command.length >= 4) {
            String volValue = command[3];
            try {
                volume = Float.parseFloat(volValue);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Volume value is not a number:" + e);
            }
        }
        if(command.length >= 5) {
            String pitchValue = command[4];
            try {
                pitch = Float.parseFloat(pitchValue);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Pitch value is not a number:" + e);
            }
        }
        if(command.length >= 6 && command[5].equals("loop")) {
            loop = true;
        }
        ResourceLocation soundRes = ResourceLocation.withDefaultNamespace(name);
        SoundEvent sound = SoundEvent.createVariableRangeEvent(soundRes);
        storyHandler.playSound(sound, volume, pitch, loop, soundType);
        sendDebugDetails();
        return true;
    }

    @Override
    void sendDebugDetails() {
        NarrativeCraftMod.LOG.info(Translation.message("debug.song/sfx", name, volume, pitch, loop).getString());
        if(storyHandler.isDebugMode()) {
            Minecraft.getInstance().player.displayClientMessage(Translation.message("debug.song/sfx", name, volume, pitch, loop), false);
        }
    }

    public enum SoundType {
        SONG,
        SFX
    }
}
