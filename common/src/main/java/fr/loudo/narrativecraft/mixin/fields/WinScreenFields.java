package fr.loudo.narrativecraft.mixin.fields;

import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WinScreen.class)
public interface WinScreenFields {

    @Invoker void callAddCreditsLine(Component text, boolean centered, boolean narrate);
    @Accessor float getUnmodifiedScrollSpeed();

}
