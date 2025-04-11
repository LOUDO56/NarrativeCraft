package fr.loudo.narrativecraft.mixin.fields;

import net.minecraft.world.entity.Display;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Display.ItemDisplay.class)
public interface ItemDisplayFields
{
    @Invoker void callSetItemStack(ItemStack itemStack);
}
