package fr.loudo.narrativecraft.mixin.fields;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Display.TextDisplay.class)
public interface TextDisplayFields
{
    @Invoker void callSetText(Component t);
    @Invoker void callSetLineWidth(int lineWidth);
    @Invoker void callSetBackgroundColor(int backgroundColor);
    @Invoker void callSetTextOpacity(byte textOpacity);
}
