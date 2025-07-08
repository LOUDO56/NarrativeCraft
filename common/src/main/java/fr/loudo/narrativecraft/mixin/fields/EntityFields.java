package fr.loudo.narrativecraft.mixin.fields;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityFields
{
    @Accessor static EntityDataAccessor<Byte> getDATA_SHARED_FLAGS_ID() { return null; }
}