package fr.loudo.narrativecraft.mixin.fields;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Font.class)
public interface FontFields {

    @Accessor boolean getFilterFishyGlyphs();
    @Invoker
    FontSet callGetFontSet(ResourceLocation fontLocation);

}
