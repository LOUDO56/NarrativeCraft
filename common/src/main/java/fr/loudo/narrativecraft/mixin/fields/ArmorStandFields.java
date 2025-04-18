package fr.loudo.narrativecraft.mixin.fields;

import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ArmorStand.class)
public interface ArmorStandFields {
    @Invoker void callSetSmall(boolean small);
}
