package fr.loudo.narrativecraft.narrative.story.inkAction;

import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.TypedSoundInstance;
import fr.loudo.narrativecraft.narrative.story.inkAction.enums.FadeCurrentState;
import fr.loudo.narrativecraft.narrative.story.inkAction.enums.InkTagType;
import fr.loudo.narrativecraft.narrative.story.inkAction.validation.ErrorLine;
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
    private FadeCurrentState fadeCurrentState;
    private long startTime, pauseStartTime;
    private TypedSoundInstance soundInstance;

    public SongSfxInkAction(StoryHandler storyHandler, String command) {
        super(storyHandler, null, command);
        if(command.contains("start")) {
            inkTagType = InkTagType.SONG_SFX_START;
        } else if(command.contains("stop")) {
            inkTagType = InkTagType.SONG_SFX_STOP;
        }
        if(command.contains("song")) {
            soundType = SoundType.SONG;
        } else if(command.contains("sfx")) {
            soundType = SoundType.SFX;
        }
    }

    @Override
    public InkActionResult execute() {
        if(command.length < 3) return InkActionResult.error(this.getClass(), Translation.message("validation.missing_sound_name").getString());
        fadeTime = 0;
        name = command[2];
        if(name.equals("all")) {
            if(soundType == null) {
                storyHandler.stopAllSound();
            } else {
                storyHandler.stopAllSoundByType(soundType);
            }
            return InkActionResult.pass();
        }
        if(inkTagType == InkTagType.SONG_SFX_START) {
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
                return InkActionResult.error(this.getClass(), Translation.message("validation.number", command[3]).getString());
            }
        }
        if(command.length >= 5 && isStart) {
            String pitchValue = command[4];
            try {
                pitch = Float.parseFloat(pitchValue);
            } catch (NumberFormatException e) {
                return InkActionResult.error(this.getClass(), Translation.message("validation.number", command[4]).getString());
            }
        }
        if(command.length >= 6 && isStart && (command[5].equals("true") || command[5].equals("false"))) {
            loop = Boolean.parseBoolean(command[5]);
        }
        if(command.length >= 8) {
            if(isStart && command[6].equals("fadein")) {
                fadeCurrentState = FadeCurrentState.FADE_IN;
                fadeTime = Double.parseDouble(command[7]);
            }
        }
        if(command.length >= 3 && !isStart) {
            if(command.length >= 4 && command[3].equals("fadeout")) {
                fadeCurrentState = FadeCurrentState.FADE_OUT;
                fadeTime = Double.parseDouble(command[4]);
            } else {
                fadeCurrentState = null;
                fadeTime = 0;
            }
        }
        ResourceLocation soundRes;
        if(name.contains(":")) {
            String[] soundNameSplit = name.split(":");
            String namespace = soundNameSplit[0];
            String path = soundNameSplit[1];
            soundRes = ResourceLocation.fromNamespaceAndPath(namespace, path);
        } else {
            soundRes = ResourceLocation.withDefaultNamespace(name);
        }
        SoundEvent sound = SoundEvent.createVariableRangeEvent(soundRes);
        if(isStart) {
            soundInstance = storyHandler.playSound(sound, volume, pitch, loop, soundType);
            if(fadeCurrentState == FadeCurrentState.FADE_IN) {
                Minecraft.getInstance().getSoundManager().setVolume(soundInstance, 0);
            }
        } else {
            if(fadeCurrentState == null && fadeTime == 0) {
                storyHandler.stopSound(sound);
                return InkActionResult.pass();
            }
            SongSfxInkAction currentSfxSong = storyHandler.getInkActionList().stream()
                    .filter(inkAction -> inkAction instanceof SongSfxInkAction && inkAction.getName().equals(this.name))
                    .map(inkAction -> (SongSfxInkAction) inkAction)
                    .findFirst()
                    .orElse(null);
            if(currentSfxSong == null) return InkActionResult.pass();
            this.soundInstance = currentSfxSong.soundInstance;
            this.volume = currentSfxSong.volume;
            this.pitch = currentSfxSong.pitch;
            storyHandler.getInkActionList().remove(currentSfxSong);
        }
        sendDebugDetails();
        storyHandler.getInkActionList().add(this);
        return InkActionResult.pass();
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
            if(fadeCurrentState == FadeCurrentState.FADE_IN) {
                newVolume = MathUtils.lerp(0, volume, t);
            } else if(fadeCurrentState == FadeCurrentState.FADE_OUT) {
                newVolume = MathUtils.lerp(volume, 0, t);
            }
            Minecraft.getInstance().getSoundManager().setVolume(soundInstance, (float) newVolume);
            if(t >= 1.0 && fadeCurrentState == FadeCurrentState.FADE_OUT) {
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
                    lineText, false
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
                        lineText,
                        false
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
                        lineText,
                        false
                );
            }
        }
        if(command.length >= 6 && isStart) {
            if(!command[5].equals("true") && !command[5].equals("false")) {
                return new ErrorLine(
                        line,
                        scene,
                        Translation.message("validation.sound_loop").getString(),
                        lineText,
                        false
                );
            }
        }
        if(command.length >= 8) {
            if(isStart && !command[6].equals("fadein")) {
                return new ErrorLine(
                        line,
                        scene,
                        Translation.message("validation.sound_start_fade_in").getString(),
                        lineText,
                        false
                );
            }
        }
        if(!isStart) {
            if(command.length >= 4 && !command[3].equals("fadeout")) {
                return new ErrorLine(
                        line,
                        scene,
                        Translation.message("validation.sound_start_fade_out").getString(),
                        lineText,
                        false
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
                        lineText,
                        false
                );
            }
        }

        String soundName = command[2];
        ResourceLocation soundRes = ResourceLocation.withDefaultNamespace(soundName);
        WeighedSoundEvents registration = Minecraft.getInstance().getSoundManager().getSoundEvent(soundRes);

        if(registration == null) {
            return new ErrorLine(
                    line,
                    scene,
                    Translation.message("validation.sound", soundName).getString(),
                    lineText, true
            );
        }

        return null;
    }

    public SimpleSoundInstance getSimpleSoundInstance() {
        return soundInstance;
    }

    public FadeCurrentState getFadeCurrentState() {
        return fadeCurrentState;
    }

    public enum SoundType {
        SONG,
        SFX
    }

}
