package fr.loudo.narrativecraft.narrative.story.inkAction;

import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.TypedSoundInstance;
import fr.loudo.narrativecraft.utils.MathUtils;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class SongSfxInkAction extends InkAction {

    private SoundType soundType;
    private boolean loop, isStart, isPaused;
    private float volume, pitch;
    private double fadeTime, t;
    private StoryHandler.FadeCurrentState fadeCurrentState;
    private long startTime, pauseStartTime;
    private TypedSoundInstance soundInstance;

    public SongSfxInkAction() {}

    public SongSfxInkAction(StoryHandler storyHandler, SoundType soundType) {
        super(storyHandler);
        this.soundType = soundType;
    }

    @Override
    public InkActionResult execute(String[] command) {
        fadeTime = 0;
        if(command[1].equals("start")) {
            name = command[2];
            loop = false;
            volume = 1.0F;
            pitch = 1.0F;
            isStart = true;
        } else {
            isStart = false;
        }
        t = 0;
        startTime = System.currentTimeMillis();
        if(command.length >= 4 && isStart) {
            String volValue = command[3];
            try {
                volume = Float.parseFloat(volValue);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Volume value is not a number:" + e);
            }
        }
        if(command.length >= 5 && isStart) {
            String pitchValue = command[4];
            try {
                pitch = Float.parseFloat(pitchValue);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Pitch value is not a number:" + e);
            }
        }
        if(command.length >= 6 && isStart && (command[5].equals("true") || command[5].equals("false"))) {
            loop = Boolean.parseBoolean(command[5]);
        }
        if(command.length >= 8) {
            if(isStart && command[6].equals("fadein")) {
                fadeCurrentState = StoryHandler.FadeCurrentState.FADE_IN;
                fadeTime = Double.parseDouble(command[7]);
            }
        }
        if(command.length >= 3 && !isStart) {
            if(command.length >= 4 && command[3].equals("fadeout")) {
                fadeCurrentState = StoryHandler.FadeCurrentState.FADE_OUT;
                fadeTime = Double.parseDouble(command[4]);
            } else {
                fadeCurrentState = null;
                fadeTime = 0;
            }
        }
        ResourceLocation soundRes = ResourceLocation.withDefaultNamespace(name);
        SoundEvent sound = SoundEvent.createVariableRangeEvent(soundRes);
        if(isStart) {
            soundInstance = storyHandler.playSound(sound, volume, pitch, loop, soundType);
            if(fadeCurrentState == StoryHandler.FadeCurrentState.FADE_IN) {
                Minecraft.getInstance().getSoundManager().setVolume(soundInstance, 0);
            }
        } else if(!isStart && fadeCurrentState == null && fadeTime == 0) {
            storyHandler.stopSound(sound);
        }
        sendDebugDetails();
        storyHandler.getInkActionList().add(this);
        return InkActionResult.PASS;
    }

    public void applyFade() {
        Minecraft minecraft = Minecraft.getInstance();
        long now = System.currentTimeMillis();
        long elapsedTime = now - startTime;
        long endTime = (long) (fadeTime * 1000L);
        if(minecraft.isPaused() && !isPaused) {
            isPaused = true;
            pauseStartTime = now;
        } else if(!minecraft.isPaused() && isPaused) {
            isPaused = false;
            startTime += now - pauseStartTime;
        }
        if(!isPaused) {
            t = Math.min((double) elapsedTime / endTime, 1.0);
            double newVolume = 0;
            if(fadeCurrentState == StoryHandler.FadeCurrentState.FADE_IN) {
                newVolume = MathUtils.lerp(0, volume, t);
            } else if(fadeCurrentState == StoryHandler.FadeCurrentState.FADE_OUT) {
                newVolume = MathUtils.lerp(volume, 0, t);
            }
            Minecraft.getInstance().getSoundManager().setVolume(soundInstance, (float) newVolume);
            if(t >= 1.0 && fadeCurrentState == StoryHandler.FadeCurrentState.FADE_OUT) {
                Minecraft.getInstance().getSoundManager().stop(soundInstance);
            }
        }
    }

    public boolean isDoneFading() {
        return t >= 1.0;
    }


    @Override
    void sendDebugDetails() {
        if(storyHandler.isDebugMode()) {
            if(isStart) {
                Minecraft.getInstance().player.displayClientMessage(Translation.message("debug.song/sfx.start", soundInstance.getSoundType().name(), name, volume, pitch, loop, fadeCurrentState == null ? "null" : fadeCurrentState.name(), fadeTime), false);
            } else {
                Minecraft.getInstance().player.displayClientMessage(Translation.message("debug.song/sfx.stop", soundInstance.getSoundType().name(), name, fadeCurrentState == null ? "null" : fadeCurrentState.name(), fadeTime), false);
            }
        }
    }

    @Override
    public ErrorLine validate(String[] command, int line, String lineText, Scene scene) {

        if(command.length < 3) {
            return new ErrorLine(
                    line,
                    scene,
                    Translation.message("validation.missing_sound_name").getString(),
                    lineText
            );
        }

        String soundName = command[2];
        ResourceLocation soundRes = ResourceLocation.withDefaultNamespace(soundName);
        WeighedSoundEvents registration = Minecraft.getInstance().getSoundManager().getSoundEvent(soundRes);

        if(registration == null) {
            return new ErrorLine(
                    line,
                    scene,
                    Translation.message("validation.sound", soundName).getString(),
                    lineText
            );
        }

        if(command[1].equals("start")) {
            isStart = true;
        } else {
            isStart = false;
        }

        if(command.length >= 4 && isStart) {
            String volValue = command[3];
            try {
                Float.parseFloat(volValue);
            } catch (NumberFormatException e) {
                return new ErrorLine(
                        line,
                        scene,
                        Translation.message("validation.number", Translation.message("global.volume")).getString(),
                        lineText
                );
            }
        }
        if(command.length >= 5 && isStart) {
            String pitchValue = command[4];
            try {
                Float.parseFloat(pitchValue);
            } catch (NumberFormatException e) {
                return new ErrorLine(
                        line,
                        scene,
                        Translation.message("validation.number", Translation.message("global.pitch")).getString(),
                        lineText
                );
            }
        }
        if(command.length >= 6 && isStart) {
            if(!command[5].equals("true") && !command[5].equals("false")) {
                return new ErrorLine(
                        line,
                        scene,
                        Translation.message("validation.sound_loop").getString(),
                        lineText
                );
            }
        }
        if(command.length >= 8) {
            if(isStart && !command[6].equals("fadein")) {
                return new ErrorLine(
                        line,
                        scene,
                        Translation.message("validation.sound_start_fade_in").getString(),
                        lineText
                );
            }
        }
        if(!isStart) {
            if(command.length >= 4 && !command[3].equals("fadeout")) {
                return new ErrorLine(
                        line,
                        scene,
                        Translation.message("validation.sound_start_fade_out").getString(),
                        lineText
                );
            }
        }

        if(command.length >= 8) {
            try {
                Double.parseDouble(command[7]);
            } catch (NumberFormatException e) {
                return new ErrorLine(
                        line,
                        scene,
                        Translation.message("validation.number", command[6].toUpperCase()).getString(),
                        lineText
                );
            }
        }


        return null;
    }

    public SimpleSoundInstance getSimpleSoundInstance() {
        return soundInstance;
    }

    public StoryHandler.FadeCurrentState getFadeCurrentState() {
        return fadeCurrentState;
    }

    public enum SoundType {
        SONG,
        SFX
    }

}
