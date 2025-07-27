package fr.loudo.narrativecraft.mixin.fields;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Boat.class)
public interface AbstractBoatFields
{
    @Accessor static EntityDataAccessor<Boolean> getDATA_ID_PADDLE_LEFT() { return null; }
    @Accessor static EntityDataAccessor<Boolean> getDATA_ID_PADDLE_RIGHT() { return null; }
    @Accessor static EntityDataAccessor<Integer> getDATA_ID_BUBBLE_TIME() { return null; }
}