package fr.loudo.narrativecraft.mixin.fields;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityFields
{
    @Accessor static EntityDataAccessor<Byte> getDATA_LIVING_ENTITY_FLAGS() { return null; }
}