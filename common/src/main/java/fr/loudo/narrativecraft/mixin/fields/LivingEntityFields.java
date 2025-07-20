package fr.loudo.narrativecraft.mixin.fields;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityFields
{
    @Invoker ItemEntity callCreateItemStackToDrop(ItemStack stack, boolean randomizeMotion, boolean includeThrower);
    @Accessor static EntityDataAccessor<Byte> getDATA_LIVING_ENTITY_FLAGS() { return null; }
}