package fr.loudo.narrativecraft.narrative.story;

import fr.loudo.narrativecraft.narrative.story.inkAction.SongSfxInkAction;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

public class TypedSoundInstance extends SimpleSoundInstance {

    private final SongSfxInkAction.SoundType soundType;

    public TypedSoundInstance(ResourceLocation location, SoundSource source, float volume, float pitch, boolean looping, SongSfxInkAction.SoundType soundType) {
        super(location, source, volume, pitch, SoundInstance.createUnseededRandom(), looping, 0, SoundInstance.Attenuation.NONE, 0, 0, 0, true);
        this.soundType = soundType;
    }

    public SongSfxInkAction.SoundType getSoundType() {
        return soundType;
    }
}
