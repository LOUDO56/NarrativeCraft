package fr.loudo.narrativecraft.mixin.fields;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractHorse.class)
public interface AbstractHorseFields
{
	@Accessor static EntityDataAccessor<Byte> getDATA_ID_FLAGS() { return null; }
    @Accessor static int getFLAG_STANDING() { return 0; }
}