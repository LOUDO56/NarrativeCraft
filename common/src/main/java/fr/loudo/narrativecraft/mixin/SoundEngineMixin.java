package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.audio.VolumeAudio;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin implements VolumeAudio {
    @Shadow private boolean loaded;

    @Shadow @Final private Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel;

    @Shadow protected abstract float calculateVolume(SoundInstance sound);

    // Code owned by Mojang. Don't exist on 1.21.1, so I had to redo it.
    @Override
    public void setVolume(SoundInstance soundInstance, float volume) {
        if (this.loaded) {
            ChannelAccess.ChannelHandle channelHandle = this.instanceToChannel.get(soundInstance);
            if (channelHandle != null) {
                channelHandle.execute((channel) -> channel.setVolume(volume * this.calculateVolume(soundInstance)));
            }
        }
    }
}
