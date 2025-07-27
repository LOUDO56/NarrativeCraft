package fr.loudo.narrativecraft.audio;

import net.minecraft.client.resources.sounds.SoundInstance;

public interface VolumeAudio {
    void setVolume(SoundInstance soundInstance, float volume);
}
