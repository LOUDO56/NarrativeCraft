package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.audio.VolumeAudio;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SoundManager.class)
public class SoundManagerMixin implements VolumeAudio {
    @Shadow @Final private SoundEngine soundEngine;

    @Override
    public void setVolume(SoundInstance soundInstance, float volume) {
        ((VolumeAudio)this.soundEngine).setVolume(soundInstance, volume);
    }
}
