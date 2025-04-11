package fr.loudo.narrativecraft.mixin.fields;

import com.mojang.math.Transformation;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Display.class)
public interface DisplayFields
{
    @Invoker void callSetBillboardConstraints(Display.BillboardConstraints billboardConstraints);
    @Invoker void callSetTransformation(Transformation transformation);
}


